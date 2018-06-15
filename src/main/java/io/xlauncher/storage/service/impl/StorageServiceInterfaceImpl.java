package io.xlauncher.storage.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.dao.StorageDaoInterface;
import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.entity.ResultEntity;
import io.xlauncher.storage.entity.VolumeEntity;
import io.xlauncher.storage.entity.enums.DeployStorageEnum;
import io.xlauncher.storage.service.StorageServiceInterface;
import io.xlauncher.storage.utils.ReadFilesUtils;
import io.xlauncher.storage.utils.ReadPropertiesUtils;
import io.xlauncher.storage.utils.SshUtils;
import io.xlauncher.storage.utils.TarFileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:50
 */
@Service
public class StorageServiceInterfaceImpl implements StorageServiceInterface {
    //记录日志
    private static Logger logger = Logger.getLogger(StorageServiceInterfaceImpl.class);

    //实例化dao
    @Autowired
    private StorageDaoInterface storageDao;

    //获取ssh服务的实例
    @Autowired
    private SshUtils ssh;
    //获取配置文件
    private ReadPropertiesUtils properties = ReadPropertiesUtils.getInstance("storage.properties");

    //读取文件操作
    @Autowired
    private ReadFilesUtils readFilesUtils;

    //打包文件操作
    @Autowired
    private TarFileUtils tarFileUtils;

    /**
     * 获取存储卷列表
     * @param user
     * @param domain
     * @return
     */
    @Override
    public List<VolumeEntity> getVolumes(String user, String domain) throws Exception{

        return null;
    }

    /**
     * 初始化存储节点
     * @param entities
     * @param type
     * @throws Exception
     */
    @Override
    public void initStorageNode(List<HostEntity> entities, String domain, String type) throws Exception {

        //将主机纳管到k8s集群中，作为存储节点
        entities.parallelStream().forEach(x -> {
            ssh.execute("curl -ssl -k https://8.16.0.52:30402/joinSlaveNode.sh | sh -s  8.16.0.52:30402 " +
                    "54ab26.8b8abe7132019134 8.16.0.52:30443 8.16.0.52:30400 " + domain.toLowerCase(),x);
        });

        //根据部署的存储系统不同,生成不同的部署文件，拷贝不同的镜像文件到存储节点中
        if (properties.getProperties("storage.type.gfs").equals(type)){
            initGfsSystem(entities, domain);
        }else if (properties.getProperties("storage.type.ceph").equals(type)){
            initCephSystem(entities,domain);
        }
    }

    /**
     * 查询当前租户下是否已经部署存储服务系统
     * @param domain
     * @return
     * @throws Exception
     */
    @Override
    public ResultEntity checkInitStorage(String domain) throws Exception {

        //设置k8som节点的信息
        HostEntity entity = ssh.getOmHost();

        //查询是否安装了glusterfs
        String glusterfs = ssh.execute("kubectl get daemonset -n "+ domain.toLowerCase() + "|grep glusterfs|wc -l",
                entity);
        String heketi = ssh.execute("kubectl get deployment -n "+ domain.toLowerCase() +"|grep heketi|wc -l",
                entity);

        boolean isGlusterfs = false;

        if ("1" .equals(glusterfs.trim()) && "1".equals(heketi.trim())) {
            isGlusterfs = true;
        }

        //查询是否安装了ceph
        String cephOsd = ssh.execute("kubectl get daemonset -n " + domain.toLowerCase() + "|grep ceph-osd|wc -l",
                entity);
        String cephMgr = ssh.execute("kubectl get deployment -n " + domain.toLowerCase() + "|grep ceph-mgr|wc -l",
                entity);
        String cephMon = ssh.execute("kubectl get daemonset -n " + domain.toLowerCase() + "|grep ceph-mon|wc -l",
                entity);

        boolean isCeph =false;

        if ("1".equals(cephOsd.trim()) && "1".equals(cephMgr.trim()) && "1".equals(cephMon.trim())) {
            isCeph = true;
        }

        ResultEntity response = new ResultEntity();

        if (isGlusterfs || isCeph) {
            response.setCode(200);
            response.setMessage("storage system has been installed in domain");
        }else {
            response.setCode(400);
            response.setMessage("storage system has not been installed in domain");
        }
        return response;
    }

    /**
     * 初始化glusterfs存储系统
     * @param entities
     * @param domain
     */
    private void initGfsSystem(List<HostEntity> entities, String domain) {
        //创建复制文件在目标主机中的存储路径

        String filePath = UUID.randomUUID().toString().replace("-","");
        String target = properties.getProperties("storage.host.file.store.path") + filePath + "/";
        entities.parallelStream().forEach(x -> {ssh.execute("mkdir -p " + target, x);});

        //复制文件在Java服务器上的存储路径
        String source = properties.getProperties("storage.host.file.send.gfs.path");

        //准备初始化GFS节点脚本
        File file = new File(source + filePath);
        if (!file.exists()){
            file.mkdirs();
        }

        String prepare = readFilesUtils.readFileToString(DeployStorageEnum.GFS_KUBE_PREPARE.getName(),filePath);

        readFilesUtils.writeFile(prepare,source + filePath + "/" + DeployStorageEnum.GFS_KUBE_PREPARE.getName());

        //开始复制文件,并执行初始化操作
        entities.parallelStream().forEach(x -> {
            ssh.copyFile(x,target,source,"gfs-image.tar.gz", "glusterfs-fuse.tar.gz", filePath + "/" +
                    DeployStorageEnum.GFS_KUBE_PREPARE.getName());
            ssh.execute("chmod +x " + target + "prepare-gfs.sh",x);
            ssh.execute("/bin/bash " + target + "prepare-gfs.sh",x);
            ssh.execute("rm -rf " + target,x);
        });

        createDeployGfsFile(entities, domain);
    }

    /**
     * 初始化ceph存储系统
     * @param entities
     * @param domain
     */
    private void initCephSystem(List<HostEntity> entities, String domain){
        //用于生成动态的存储目录,在目标主机和Java服务器上存储临时文件
        String uuid = UUID.randomUUID().toString().replace("-","");
        //在目标主机中用于存储复制文件，包括镜像文件以及ceph-common安装文件和镜像加载文件
        String storePath = properties.getProperties("storage.host.file.store.path") + uuid + "/";

        //修改prepare-ceph.sh文件
        String prepare = readFilesUtils.readFileToString(DeployStorageEnum.CEPH_INSTALL_PREPARE.getName(),uuid);
        readFilesUtils.writeFile(prepare, properties.getProperties("storage.host.file.send.ceph.path") +
                "prepare-node.sh");
        //需要拷贝的文件列表
        String[] files = {"ceph-images.tar.gz", "ceph-common.tar.gz", "prepare-node.sh"};
        //复制镜像文件,ceph-common安装文件到各个主机上,并初始化部署环境
        entities.parallelStream().forEach(x -> {
            //在目标主机上创建文件夹
            ssh.execute("mkdir -p " + storePath, x);
            ssh.copyFile(x, storePath,properties.getProperties("storage.host.file.send.ceph.path"),files);

            //解压镜像文件,ceph-common文件，并进行节点初始化操作
            //执行脚本，初始化ceph部署节点
            ssh.execute("chmod +x " + storePath + "prepare-node.sh", x);
            ssh.execute("bash -c " + storePath + "prepare-node.sh ", x);
            ssh.execute("rm -rf " + storePath, x);
        });

        //复制部署ceph的源文件的路径
        String source = properties.getProperties("storage.deploy.file.ceph.path");
        //Java服务器存储文件的路径
        String target = properties.getProperties("storage.host.file.send.ceph.path") + uuid + "/";

        //在Java服务器上创建文件夹，用于存储拷贝到主机上的ceph部署文件
        File file = new File(target);
        if (!file.exists()) {
            file.mkdirs();
        }

        //将需要的文件拷贝到目标文件夹
        readFilesUtils.copyFile(source, target);

        //修改values.yaml文件，并写入到目标文件夹下
        String values = readFilesUtils.readFileToString(DeployStorageEnum.CEPH_INSTALL_VALUES.getName(),
                String.valueOf(ssh.getOmApiPort()), ssh.getOmIP(),domain.toLowerCase());
        readFilesUtils.writeFile(values,target + DeployStorageEnum.CEPH_INSTALL_VALUES.getName());

        //修改rbac文件，写入到文件中
        String rbac = readFilesUtils.readFileToString(DeployStorageEnum.CEPH_INSTALL_RBAC.getName(),domain.toLowerCase());
        readFilesUtils.writeFile(rbac,target + DeployStorageEnum.CEPH_INSTALL_RBAC.getName());

        //修改overrides文件，并写入到文件中
        String overrides = readFilesUtils.readFileToString(DeployStorageEnum.CEPH_INSTALL_OVERRIDES.getName());
        String over = readFilesUtils.addCephDevices(overrides,entities);
        readFilesUtils.writeFile(over, target + DeployStorageEnum.CEPH_INSTALL_OVERRIDES.getName());

        /**
         *修改初始化文件install-ceph，并写入到文件中本地文件夹中
         */
        //记录node名称
        StringBuffer nodes = new StringBuffer();
        entities.parallelStream().forEachOrdered(x -> {
            String name = x.getName();
            nodes.append(name).append(" ");
        });

        //节点需要打上的标签
        StringBuffer label = new StringBuffer();

        for (String lab : entities.get(0).getDevices()) {
            label.append("ceph-osd-device").append(lab.replace("/","-")).append("=enabled ");
        }

        label.append("ceph-mon=enabled ceph-mgr=enabled ceph-osd=enabled ceph-app=" + domain.toLowerCase());

        //记录ceph-osd需要启动的数量
        int osd = entities.size() * entities.get(0).getDevices().length;
        //记录ceph-mon的数量
        int mon = entities.size();
        String[] shellParames = {uuid, nodes.toString(), domain.toLowerCase(), label.toString(), String.valueOf(osd),
                String.valueOf(mon)};
        //读取install-ceph。sh脚本，并替换其中的参数
        String shell = readFilesUtils.readFileToString(DeployStorageEnum.CEPH_INSTALL_SHELL.getName(),shellParames);
        //将shell脚本写入到文件中
        readFilesUtils.writeFile(shell,target + DeployStorageEnum.CEPH_INSTALL_SHELL.getName());

        //将文件夹打包
        tarFileUtils.tarFiles(target + "ceph", target, "ceph-helm.tar",true);

        //将文件复制到k8s的om节点中
        HostEntity entity = ssh.getOmHost();

        ssh.execute("mkdir -p /tmp/" + uuid, entity);
        ssh.copyFile(entity,"/tmp/" + uuid + "/", target, "ceph-helm.tar");

        //解压缩ceph部署文件
        ssh.execute("tar xvf /tmp/" + uuid + "/ceph-helm.tar -C /tmp/" + uuid, entity);

        //为install-ceph.sh增加执行权限
        ssh.execute("chmod +x /tmp/" + uuid + "/install-ceph.sh", entity);

        //执行install-ceph.sh部署脚本
        //安装完成以后删除文件
        ssh.execute("bash -c /tmp/" + uuid + "/install-ceph.sh", entity);

        file.delete();
    }

    /**
     * 用于生成部署Glusterfs需要的yaml文件以及json文件
     * @param entities
     * @param domain
     */
    private void createDeployGfsFile(List<HostEntity> entities, String domain) {

        //需要拷贝的文件的列表
        String[] files = {DeployStorageEnum.GFS_KUBE_DAEMON.getName(),DeployStorageEnum.GFS_KUBE_DEPLOY.getName(),
                DeployStorageEnum.GFS_KUBE_DEPLOYMENT.getName(),DeployStorageEnum.GFS_KUBE_HEKETI.getName(),
                DeployStorageEnum.GFS_KUBE_HEKETI_JSON.getName(),DeployStorageEnum.GFS_KUBE_PVC.getName(),
                DeployStorageEnum.GFS_KUBE_S3.getName(),DeployStorageEnum.GFS_KUBE_STORAGE.getName(),
                DeployStorageEnum.GFS_KUBE_INIT.getName(),DeployStorageEnum.GFS_KUBE_SHELL.getName(),
                DeployStorageEnum.GFS_KUBE_TOPOLOGY.getName()};
        //用于临时存储文件的目录
        String uuid = UUID.randomUUID().toString().replace("-","");

        String storePath = properties.getProperties("storage.deploy.file.gfs.store.path",uuid) + "glusterfs/";

        String[] parames = {domain.toLowerCase(),String.valueOf(ssh.getOmApiPort()),ssh.getOmIP()};
        StringBuffer nodes = new StringBuffer();
        entities.parallelStream().forEachOrdered(x -> {
            nodes.append(x.getName()).append(" ");
        });
        //读取并配置部署文件
        for (String file : files) {
            String info = "";
            if (file.equals(DeployStorageEnum.GFS_KUBE_INIT.getName())) {
                info = readFilesUtils.readFileToString(file,"/tmp/" + uuid,nodes.toString(), domain.toLowerCase(),
                        domain.toLowerCase());
            }else if (file.equals(DeployStorageEnum.GFS_KUBE_SHELL.getName())){
                info = readFilesUtils.readFileToString(file);
            }else if (file.equals(DeployStorageEnum.GFS_KUBE_TOPOLOGY.getName())){
                String json = readFilesUtils.readFileToString(DeployStorageEnum.GFS_KUBE_TOPOLOGY.getName());
                JSONObject jsonObject = readFilesUtils.addHostEntity(json,entities);
                info = JSONObject.toJSONString(jsonObject,true);
            }else {
                info = readFilesUtils.readFileToString(file, parames);
            }
            readFilesUtils.writeFile(info, storePath + file);
        }

        tarFileUtils.tarFiles(storePath,properties.getProperties("storage.deploy.file.gfs.store.path",uuid),
                properties.getProperties("storage.deploy.file.gfs.tar.name"), true);

        HostEntity entity = ssh.getOmHost();

        ssh.execute("mkdir -p /tmp/" + uuid,entity);
        ssh.copyFile(entity, "/tmp/" + uuid + "/",properties.getProperties("storage.deploy.file.gfs.store.path",
                 uuid), properties.getProperties("storage.deploy.file.gfs.tar.name"));

        ssh.execute("tar xvf /tmp/" + uuid + "/" + properties.getProperties("storage.deploy.file.gfs.tar.name")
                + " -C /tmp/" + uuid + "/",entity);
        ssh.execute("chmod +x /tmp/" + uuid + "/" + DeployStorageEnum.GFS_KUBE_INIT.getName(),entity);
        ssh.execute("/tmp/" + uuid + "/" + DeployStorageEnum.GFS_KUBE_INIT.getName() + " >> /var/log/" +
                "install-glusterfs-" + String.valueOf(new Date().getTime()) + ".log",entity);
        ssh.execute("rm -rf /tmp/" + uuid, entity);
        return;
    }
}

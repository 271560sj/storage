package io.xlauncher.storage.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.dao.StorageDaoInterface;
import io.xlauncher.storage.entity.HostEntity;
import io.xlauncher.storage.entity.ResponseEntity;
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
import java.util.ArrayList;
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

    //实例化FTP服务
//    @Autowired
//    private FtpUtils ftp;

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

    //获取om1节点的主机IP地址，主机端口，主机用户，主机密码
    private static String k8sApiIP = "8.16.0.70";//System.getenv("KUBERNETES_NODE_IP");
    private static Integer k8sApiPort = 30443;//Integer.parseInt(System.getenv("KUBERNETES_NODE_PORT"));
    private static String k8sApiUser = "root";//System.getenv("KUBERNETES_NODE_USER");
    private static String k8sApiPassword = "root";//System.getenv("KUBERNETES_NODE_PASSWD");

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
        for (HostEntity entity : entities){
            storageDao.addStorageNode(entity,domain);
        }

        //根据部署的存储系统不同,生成不同的部署文件，拷贝不同的镜像文件到存储节点中
        if (properties.getProperties("storage.type.gfs").equals(type)){
            initGfsSystem(entities, domain);
        }else if (properties.getProperties("storage.type.ceph").equals(type)){
            //todo 部署ceph
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
    public ResponseEntity checkInitStorage(String domain) throws Exception {

        //设置k8som节点的信息
        HostEntity entity = new HostEntity();
        entity.setIp(k8sApiIP);
        entity.setUser(k8sApiUser);
        entity.setPassword(k8sApiPassword);

        ssh.setHostEntity(entity);
        //查询是否安装了glusterfs
        String glusterfs = ssh.execute("kubectl get daemonset -n "+ domain.toLowerCase() + "|grep glusterfs|wc -l");
        String heketi = ssh.execute("kubectl get deployment -n "+ domain.toLowerCase() +"|grep heketi|wc -l");

        boolean isGlusterfs = false;

        if ("1" .equals(glusterfs.trim()) && "1".equals(heketi.trim())) {
            isGlusterfs = true;
        }

        //查询是否安装了ceph
        String cephOsd = ssh.execute("kubectl get daemonset -n " + domain.toLowerCase() + "|grep ceph-osd|wc -l");
        String cephMgr = ssh.execute("kubectl get deployment -n " + domain.toLowerCase() + "|grep ceph-mgr|wc -l");
        String cephMon = ssh.execute("kubectl get daemonset -n " + domain.toLowerCase() + "|grep ceph-mon|wc -l");

        boolean isCeph =false;

        if ("1".equals(cephOsd.trim()) && "1".equals(cephMgr.trim()) && "1".equals(cephMon.trim())) {
            isCeph = true;
        }

        ResponseEntity response = new ResponseEntity();

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
        entities.parallelStream().forEachOrdered(x -> {ssh.setHostEntity(x); ssh.execute("mkdir -p " + target);});

        //复制文件的存储路径
        String source = properties.getProperties("storage.host.file.send.gfs.path");

        //开始复制文件
//        ftp.FTPSendFile(entities, target, source, files);
        entities.parallelStream().forEachOrdered(x -> {
            ssh.setHostEntity(x);
            ssh.copyFile(target,source,"gfs-image.tar.gz");
        });
        //开始解压文件
        String cmd = "tar xvf " + target + "gfs-image.tar.gz -C " + target;
        entities.parallelStream().forEachOrdered(x -> {ssh.setHostEntity(x); ssh.execute(cmd);});

        //开始加载镜像
        String[] images = {DeployStorageEnum.GFS_IMAFE_HEKETI.getName(), DeployStorageEnum.GFS_IMAGE_GLUSTERFS.getName(),
        DeployStorageEnum.GFS_IMAGE_OBJECT.getName()};
        for (String file : images) {
            String loadCmd = "docker load -i " + target + "gfs-image/" + file;
            entities.parallelStream().forEachOrdered(x -> {ssh.setHostEntity(x); ssh.execute(loadCmd.toString());});
        }

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
        entities.parallelStream().forEachOrdered(x -> {
            ssh.setHostEntity(x);
            //在目标主机上创建文件夹
            ssh.execute("mkdir -p " + storePath);
            ssh.copyFile(storePath,properties.getProperties("storage.host.file.send.ceph.path"),files);

            //解压镜像文件,ceph-common文件，并进行节点初始化操作
            //执行脚本，初始化ceph部署节点
            ssh.execute("chmod +x " + storePath + "prepare-node.sh");
            ssh.execute("bash -c " + storePath + "prepare-node.sh ");
            ssh.execute("rm -rf " + storePath);
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
                String.valueOf(k8sApiPort), k8sApiIP,domain.toLowerCase());
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
            label.append("ceph-osd").append(lab.replace("/","-")).append("=enabled ");
        }

        label.append("ceph-mon=enabled ceph-mgr=enabled ceph-osd=enabled");

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
        HostEntity entity = new HostEntity();
        entity.setPassword(k8sApiPassword);
        entity.setUser(k8sApiUser);
        entity.setIp(k8sApiIP);

        ssh.setHostEntity(entity);
        ssh.execute("mkdir -p /tmp/" + uuid);
        ssh.copyFile("/tmp/" + uuid + "/", target, "ceph-helm.tar");

        //解压缩ceph部署文件
        ssh.execute("tar xvf /tmp/" + uuid + "/ceph-helm.tar -C /tmp/" + uuid);

        //为install-ceph.sh增加执行权限
        ssh.execute("chmod +x /tmp/" + uuid + "/install-ceph.sh");

        //执行install-ceph.sh部署脚本
        //安装完成以后删除文件
        ssh.execute("bash -c /tmp/" + uuid + "/install-ceph.sh");

        file.delete();
    }

    /**
     * 用于生成部署Glusterfs需要的yaml文件以及json文件
     * @param entities
     * @param domain
     */
    private void createDeployGfsFile(List<HostEntity> entities, String domain) {

        String[] files = {DeployStorageEnum.GFS_KUBE_DAEMON.getName(),DeployStorageEnum.GFS_KUBE_DEPLOY.getName(),
                DeployStorageEnum.GFS_KUBE_DEPLOYMENT.getName(),DeployStorageEnum.GFS_KUBE_HEKETI.getName(),
                DeployStorageEnum.GFS_KUBE_HEKETI_JSON.getName(),DeployStorageEnum.GFS_KUBE_PVC.getName(),
                DeployStorageEnum.GFS_KUBE_S3.getName(),DeployStorageEnum.GFS_KUBE_STORAGE.getName(),
                DeployStorageEnum.GFS_KUBE_INIT.getName()};
        String uuid = UUID.randomUUID().toString().replace("-","");
        String storePath = properties.getProperties("storage.deploy.file.gfs.store.path","",uuid) + "glusterfs/";

        String[] parames = {domain.toLowerCase(),String.valueOf(k8sApiPort),k8sApiIP};

        //读取并配置yaml文件
        for (String file : files) {
            String info = "";
            if (file.equals(DeployStorageEnum.GFS_KUBE_INIT.getName())) {
                info = readFilesUtils.readFileToString(file,uuid,domain.toLowerCase());
            }else {
                info = readFilesUtils.readFileToString(file, parames);
            }
            readFilesUtils.writeFile(info, storePath + file);
        }

        //读取并设置安装脚本
        String installShell = readFilesUtils.readFileToString(DeployStorageEnum.GFS_KUBE_SHELL.getName());
        readFilesUtils.writeFile(installShell,storePath + DeployStorageEnum.GFS_KUBE_SHELL.getName());

        //读取并配置json文件
        String json = readFilesUtils.readFileToString(DeployStorageEnum.GFS_KUBE_TOPOLOGY.getName());
        JSONObject jsonObject = readFilesUtils.addHostEntity(json,entities);
        readFilesUtils.writeFile(JSONObject.toJSONString(jsonObject, true),
                storePath + DeployStorageEnum.GFS_KUBE_TOPOLOGY.getName());

        tarFileUtils.tarFiles(storePath,properties.getProperties("storage.deploy.file.gfs.store.path","",uuid),
                properties.getProperties("storage.deploy.file.gfs.tar.name"), true);

        HostEntity entity = new HostEntity();
        entity.setIp(k8sApiIP);
        entity.setPassword(k8sApiPassword);
        entity.setUser(k8sApiUser);
//        entity.setPort(k8sApiPort);

        List<HostEntity> list = new ArrayList<HostEntity>();
        list.add(entity);

        List<String> tars = new ArrayList<String>();
        tars.add(properties.getProperties("storage.deploy.file.gfs.tar.name"));

        ssh.setHostEntity(entity);
        ssh.execute("mkdir -p /tmp/" + uuid);
        ssh.copyFile("/tmp/" + uuid + "/",properties.getProperties("storage.deploy.file.gfs.store.path", "",
                uuid), properties.getProperties("storage.deploy.file.gfs.tar.name"));

        ssh.execute("tar xvf /tmp/" + uuid + "/" + properties.getProperties("storage.deploy.file.gfs.tar.name")
                + " -C /tmp/" + uuid + "/");
        entities.parallelStream().forEachOrdered(x -> {
            ssh.execute("kubectl label node " + x.getName() + " apollo-domain=" + domain.toLowerCase());
        });
        ssh.execute("kubectl create namespace " + domain.toLowerCase());
        ssh.execute("chmod +x /tmp/" + uuid + "/" + DeployStorageEnum.GFS_KUBE_SHELL.getName());
        ssh.execute("chmod +x /tmp/" + uuid + "/" + DeployStorageEnum.GFS_KUBE_INIT.getName());
        String installCmd = "/tmp/" + uuid + "/" + DeployStorageEnum.GFS_KUBE_SHELL.getName() + " -n " +
                domain.toLowerCase() + " -g";
        ssh.execute(installCmd);
        return;
    }
}

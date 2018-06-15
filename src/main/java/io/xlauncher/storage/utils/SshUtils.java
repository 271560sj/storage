package io.xlauncher.storage.utils;

import ch.ethz.ssh2.*;
import io.xlauncher.storage.entity.HostEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:14
 * 用于ssh服务，向主机发送shell命令，执行
 */
@Component
public class SshUtils {

    /**
     * 记录日志
     */
    private static Logger log = Logger.getLogger(SshUtils.class);

    //获取配置文件
    private static ReadPropertiesUtils properties = ReadPropertiesUtils.getInstance("storage.properties");

    //设置cmd命令返回结果的编码格式
    private static final String DEFAULT_CHART = properties.getProperties("storage.host.cmd.charset");

    /**
     * 执行cmd命令
     * @param cmd
     * @return
     */
    public String execute(String cmd, HostEntity entity) {
        //记录返回结果
        String result = "";
        //执行命令
        try {
            Connection connection = login(entity);
            if (connection != null) {//判断登录是否成功
                //打开通话session
                Session session = connection.openSession();
                //执行cmd命令
                session.execCommand(cmd);
                //解析执行返回的结果
                result = processStdout(session.getStdout(), DEFAULT_CHART);
                if (StringUtils.isBlank(result)) {
                    result = processStdout(session.getStderr(), DEFAULT_CHART);
                }

                //关闭connection和session
                connection.close();
                session.close();
            }
        }catch (IOException e){
            log.error("SshUtils,execute,run cmd command error.", e);
        }finally {
            return result;
        }
    }

    /**
     * 登录到主机中
     * @return
     */
    private Connection login(HostEntity entity) {
        Connection connection;
        //进行登录主机操作
        try {
            //创建连接
            connection = new Connection(entity.getIp());
            connection.connect();
            //配置密码和账户，并进行登录
            boolean isSuccessed = connection.authenticateWithPassword(entity.getUser(),entity.getPassword());
            if (isSuccessed){
                return connection;
            }
        }catch (IOException e){
            log.error("SshUtils,login, login to host error.",e);
        }
        return null;

    }

    /**
     * 复制本地文件到远端主机
     * @param local
     * @param remote
     */
    public void copyFile(HostEntity entity, String remote, String local, String... files) {
        try {
            Connection connection = login(entity);
            if (connection != null ){
                SCPClient client = connection.createSCPClient();
                for (String file : files) {
                    File copyFile = new File(local + file);
                    SCPOutputStream stream = client.put(copyFile.getName(), copyFile.length(), remote,"0644");

                    FileInputStream fileInputStream = new FileInputStream(copyFile);
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    while ((len = fileInputStream.read(buffer)) != -1) {
//                        stream.write(buffer);
                        stream.write(buffer,0, len);
                        stream.flush();
                    }
                    stream.close();
                }
                connection.close();
            }
        }catch (IOException e){
            log.error("SshUtils,copyFile, copy file error", e);
        }
    }

    /**
     * 解析执行cmd命令以后返回的结果
     * @param in
     * @param charset
     * @return
     */
    private String processStdout(InputStream in, String charset) {
        //处理获取的输入流
        InputStream stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();

        //解析输入流
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout,charset));

            String line = null;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
        }catch (UnsupportedEncodingException e){
            log.error("SshUtils,processStdout,read cmd result error.", e);
        }catch (IOException e){
            log.error("SshUtils,processStdout,read cmd result error.", e);
        }finally {
            return buffer.toString();
        }
    }

    /**
     * 获取k8s集群中om管理面的主机信息
     * @return
     */
    public HostEntity getOmHost() {
        HostEntity entity = new HostEntity();

        entity.setUser(getOmUser());
        entity.setIp(getOmIP());
        entity.setPassword(getOmPasswd());
        entity.setPort(getOmApiPort());

        return entity;
    }

    /**
     * 获取om主机的IP地址
     * @return
     */
    public String getOmIP(){
//        return System.getenv("KUBERNETES_SERVICE_HOST");
        return "8.16.0.52";
    }

    /**
     * 获取om主机的用户
     * @return
     */
    public String getOmUser(){
//        return System.getenv("KUBERNETES_SERVICE_USER");
        return "root";
    }

    /**
     * 获取om主机的密码
     * @return
     */
    public String getOmPasswd(){
//        return System.getenv("KUBERNETES_SERVICE_PASSWORD");
        return "root";
    }

    /**
     * 获取om主机上API server的端口号
     * @return
     */
    public Integer getOmApiPort() {
//        return Integer.parseInt(System.getenv("KUBERNETES_SERVICE_PORT"));
        return 30443;
    }

    /**
     * 获取操作存储服务的IP地址和port
     * @param volume
     * @param domain
     * @return
     */
    public String getStorageHost(String volume, String domain) {
        //记录存储的主机IP地址
        String host = "";
        //记录存储主机的port
        String port = "";
        if (StringUtils.isBlank(volume) || StringUtils.isBlank(domain)) {
            log.error("VolumeServiceImpl, getStorageHost,type of storage or domain is null");
            return host;
        }
        //获取k8s集群om节点的信息
        HostEntity entity = getOmHost();

        //拼接查询存储IP地址的cmd
        StringBuffer ipCmd = new StringBuffer();
        //拼接查询存储port的cmd
        StringBuffer portCmd = new StringBuffer();

        if (properties.getProperties("storage.type.gfs").equals(volume.toLowerCase())) {
            //查询glusterfs的IP地址kubectl describe node $(kubectl get pod -n alan -o wide|grep heketi|awk '{print $7}')|
            // grep InternalIP|awk '{print $2}'
            ipCmd.append("kubectl describe node $(kubectl get pod -n ").append(domain.toLowerCase()).append(" -o wide|grep")
                    .append(" heketi").append("|awk '{print $7}')|grep InternalIP|awk '{print $2}'");

            //查询glusterfs服务的端口号
            //kubectl get svc -n alan|grep -w heketi|grep NodePort|awk '{print $5}'|awk -F '/' '{print $1}'|awk -F ':' '
            // {print $2}'
            portCmd.append("kubectl get svc -n ").append(domain.toLowerCase()).append("|grep -w heketi|grep NodePort|awk " +
                    "'{print $5}'|awk -F '/' '{print $1}'|awk -F ':' '{print $2}'");
        }else {
            //查询ceph的服务IP地址
            //kubectl describe endpoints -n alan ceph-mon|grep -w Addresses|awk '{print $2}'|awk -F ',' '{print $1}'
            ipCmd.append("kubectl describe endpoints -n " ).append(domain.toLowerCase()).append(" ceph-mon|grep -w ")
                    .append("Addresses|awk '{print $2}'|awk -F ',' '{print $1}'");
            //查询ceph服务的port
            //kubectl get svc -n alan|grep ceph-mon|awk '{print $5}'|awk -F ',' '{print $2}'|awk -F '/' '{print $1}'|awk
            // -F ':' '{print $2}'
            portCmd.append("kubectl get svc -n ").append(domain.toLowerCase()).append("|grep ceph-mon|awk '{print $5}'|")
                    .append("awk -F ',' '{print $2}'|awk -F '/' '{print $1}'|awk  -F ':' '{print $2}'");
        }

        host = execute(ipCmd.toString(), entity);
        port = execute(portCmd.toString(), entity);

        return "http://" + host.trim() + ":" + port.trim();
    }

}

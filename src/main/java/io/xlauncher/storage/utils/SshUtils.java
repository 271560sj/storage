package io.xlauncher.storage.utils;

import ch.ethz.ssh2.*;
import io.xlauncher.storage.entity.HostEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

    //主机实例
    private HostEntity entity;

    //ssh连接实例
//    private Connection connection;

    /**
     * 设置主机的信息
     * @param entity
     */
    public void setHostEntity(HostEntity entity) {
        this.entity = entity;
    }

    /**
     * 执行cmd命令
     * @param cmd
     * @return
     */
    public String execute(String cmd) {
        //记录返回结果
        String result = "";
        //执行命令
        try {
            Connection connection = login();
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
    private Connection login() {
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
    public void copyFile(String remote, String local, String... files) {
        try {
            Connection connection = login();
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

}

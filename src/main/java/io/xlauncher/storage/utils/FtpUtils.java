package io.xlauncher.storage.utils;

import io.xlauncher.storage.entity.HostEntity;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:14
 * 用于FTP服务，向主机发送文件
 */
@Component
public class FtpUtils {

    /**
     * 记录日志
     */
    private static Logger log = Logger.getLogger(FtpUtils.class);
    /**
     * 创建FTP的客户端
     */
    private FTPClient ftpClient;

    /**
     * 发送文件到主机
     * @param to 发送文件在主机中的保存位置
     * @param from 发送文件的路径
     * @param files 发送文件的列表
     */
    public void FTPSendFile(List<HostEntity> entities, String to, String from, List<String> files) {
//        for (HostEntity entity : entities) {
//            if (this.FTPStart(entity)) {
//                for (String file : files) {
//                    FTPUpload(to + file, from + file);
//                }
//            }
//            this.FTPClose();
//        }
        entities.parallelStream().forEachOrdered(x -> {
            if (this.FTPStart(x)) {
                files.parallelStream().forEachOrdered(file -> {FTPUpload(to + file, from + file);});
            }
            this.FTPClose();
        });
    }

    /**
     * 开始FTP服务
     * @param entity
     * @return
     */
    private boolean FTPStart(HostEntity entity) {
        //标识是否启动FTP服务成功
        boolean isSuccessed = false;

        //获取FTP客户端
        ftpClient = new FTPClient();

        try {
            //连接到主机
            ftpClient.connect(entity.getIp(), entity.getPort());
            //登录到主机
            ftpClient.login(entity.getUser(), entity.getPassword());
            //设置文件传输的类型
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            //获取连接FTP的结果
            int reply = ftpClient.getReplyCode();

            //判断连接结果是否成功
            if (FTPReply.isPositiveCompletion(reply)) {
                isSuccessed = true;
            }else {
                ftpClient.disconnect();
            }
        }catch (SocketException e){
            log.error("FtpUtils,FTPStart, start ftp server error.", e);
        }catch (IOException e){
            log.error("FtpUtils,FTPStart, start ftp server error.", e);
        }finally {
            System.out.println("ftp login status is " + isSuccessed);
            return isSuccessed;
        }
    }

    /**
     * 通过FTP服务传递文件
     * @param target 文件在主机中的保存路径
     * @param local 文件在服务器的保存路径
     * @return
     */
    private boolean FTPUpload(String target, String local) {
        //标识是否传输成功
        boolean isSuccessed = false;

        //获取文件流
        FileInputStream in = null;

        //获取文件
        File file = new File(local);

        //对文件流进行处理，传输
        try {
            in = new FileInputStream(file);

            //判断文件是否传输成功
            if (ftpClient.storeFile(target, in)) {
                isSuccessed = true;
            }
        }catch (Exception e){
            log.error("FTPUtils, FTPUpload,upload files error.", e);
        }finally {
            if (null != in) {
                try {
                    in.close();
                }catch (IOException e){
                    log.error("FTPUtils, FTPUpload,close file input stream error.", e);
                }
            }
            return isSuccessed;
        }
    }

    /**
     * 关闭FTP服务
     * @return
     */
    private boolean FTPClose() {
        //标识是否关闭服务成功
        boolean isSuccessed = false;

        //关闭FTP服务
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                isSuccessed = true;
            }catch (IOException e){
                log.error("FtpUtils,FTPClose,close ftp server error.", e);
                return isSuccessed;
            }
        }
        return isSuccessed;
    }
}

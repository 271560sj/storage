package io.xlauncher.storage.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-6 下午1:38
 */
@Component
public class TarFileUtils {

    //记录日志
    private static Logger logger = Logger.getLogger(TarFileUtils.class);

    private File fileSrc = null;

    private File dist = null;

    private TarArchiveOutputStream tarStream = null;

    private boolean delete = false;

    /**
     * 将文件夹压缩成tar包
     * @param src
     * @param target
     * @param tarName
     * @param delete
     */
    public void tarFiles(String src, String target, String tarName, boolean delete) {
        this.fileSrc = new File(src);
        this.dist = new File(target, tarName);
        this.delete = delete;
        if (fileSrc.exists()) {
            try {
                tarStream = new TarArchiveOutputStream(new FileOutputStream(dist));
            }catch (FileNotFoundException e){
                logger.error("TarFileUtils, tarFiles, tar file error", e);
            }
        }

        action(fileSrc);

        if (tarStream != null) {
            try {
                tarStream.close();
            }catch (IOException e){
                logger.error("TarFileUtils, tarFiles, tar file error", e);
            }
        }
    }

    /**
     * 递归文件夹，将所有的文件都打包
     * @param src
     */
    private void action(File src) {
        if (tarStream == null){
            logger.error("TarFileUtils, action, tar file error,file not found");
            return;
        }

        if (src.isFile()) {
            append(tarStream, src);
        }else if (src.isDirectory()){
            File[] files = src.listFiles();
            if (files != null && files.length > 0){
                for (File file : files){
                    action(file);
                }
            }
        }
    }

    /**
     * 打包文件
     * @param stream
     * @param file
     */
    private void append(TarArchiveOutputStream stream, File file) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            TarArchiveEntry entry = new TarArchiveEntry(file);
            entry.setSize(file.length());
            entry.setName(file.getAbsolutePath().substring(fileSrc.getAbsolutePath().length() + 1));
            tarStream.putArchiveEntry(entry);
            IOUtils.copy(in, tarStream);
            tarStream.flush();
            tarStream.closeArchiveEntry();
        }catch (Exception e){
            logger.error("TarFileUtils,append,append files error", e);
        }finally {
            IOUtils.closeQuietly(in);
            if (this.delete) {
                if (!file.delete()) {
                    logger.error("TarFileUtils,append,delete file error");
                }
            }
        }
    }
}

package io.xlauncher.storage.utils;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午5:27
 * 用于读取配置文件
 */
public class ReadPropertiesUtils {

    //记录日志
    private static Logger log = Logger.getLogger(ReadPropertiesUtils.class);

    //记录配置文件的目录
    private String propertiesPath = "/static/config/{0}";

    //用于实例化properties实例
    private Properties properties;

    private ReadPropertiesUtils() {
    }

    public static ReadPropertiesUtils getInstance(String file) {
        ReadPropertiesUtils instance = new ReadPropertiesUtils();
        instance.loadProperties(file);
        return instance;
    }

    /**
     * 加载配置文件
     * @param file 文件名称
     */
    public void loadProperties(String file) {
        //properties实例化
        properties = new Properties();

        //用于获取文件流
        InputStream in = null;

        try {
            //获取文件流
            in = ReadPropertiesUtils.class.getResourceAsStream(MessageFormat.format(propertiesPath, file));
            //加载配置文件
            properties.load(in);
        }catch (IOException e){
            log.error("ReadPropertiesUtils,loadProperties,read properties file error.",e);
        }finally {
            try {
                if (null != in) {
                    in.close();
                }
            }catch (IOException e){
                log.error("ReadPropertiesUtils,loadProperties,close properties file error.",e);
            }
        }
    }

    /**
     * 获取配置文件内容
     * @param key
     * @return
     */
    public String getProperties(String key) {
        return properties.getProperty(key);
    }

    /**
     * 读取配置文件的内容
     * @param key 根据key值读取配置文件
     * @param value 设置默认值，如果key不存在，或者读取的为空值，则返回该值
     * @return
     */
    public String getProperties(String key, String value) {
        //判断key的值是否存在
        if (StringUtils.isBlank(properties.getProperty(key))) {
            return value;
        }

        return properties.getProperty(key);
    }

    /**
     * 读取配置文件内容
     * @param key 获取内容的key
     * @param value 默认值
     * @param values 需要替换的值
     * @return
     */
    public String getProperties(String key, String value, String... values) {
        //判断key的值是否存在
        if (StringUtils.isBlank(properties.getProperty(key))) {
            return value;
        }

        return MessageFormat.format(properties.getProperty(key), values);
    }
}

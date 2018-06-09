package io.xlauncher.storage.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.xlauncher.storage.entity.HostEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-5 下午6:38
 */
@Component
public class ReadFilesUtils {

    //记录日志
    private static Logger logger = Logger.getLogger(ReadFilesUtils.class);
    //读取配置文件
    private static ReadPropertiesUtils properties = ReadPropertiesUtils.getInstance("storage.properties");

    /**
     * 读取yaml文件，并转换为字符串
     * @param file
     * @param parames
     * @return
     */
    public String readFileToString(String file, String... parames) {

        String yamlStr = "";
        try {
            InputStream in = ReadFilesUtils.class.getResourceAsStream(properties.getProperties("storage.deploy.file.path","", file));
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            bufferedReader.close();
            in.close();
            if (parames.length <= 0){
                yamlStr = String.valueOf(builder);
            }else {
                yamlStr = format(String.valueOf(builder), parames);
            }
        }catch (IOException e){
            logger.error("YamlConfigUtils,readYamlToString,read file error.", e);
        }finally {
            return yamlStr;
        }
    }

    /**
     * 向glusterfs heketi的topo文件中增加主机的信息
     * @param json
     * @param entities
     * @return
     */
    public JSONObject addHostEntity(String json, List<HostEntity> entities) {
        JSONObject object = JSONObject.parseObject(json);
        int i = 0;
        for (HostEntity entity : entities) {
            Map<String,String[]> hostname = new HashMap<String, String[]>();
            hostname.put("manage", new String[]{entity.getName()});
            hostname.put("storage", new String[]{entity.getIp()});
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("zone", 1);
            map.put("hostnames", hostname);
            Map<String, Object> newNode = new HashMap<String, Object>();
            newNode.put("node",map);
            newNode.put("devices",entity.getDevices());
            JSONObject jsonObject = new JSONObject(newNode);
            object.getJSONArray("clusters").getJSONObject(0).getJSONArray("nodes").fluentAdd(i ++, jsonObject);
        }

        return object;
    }

    /**
     * 用于拷贝文件
     * @param origin
     * @param target
     * @param files
     * @return
     */
    public boolean copyFile(String origin, String target, String... files) {
        boolean isSuccess = false;

        try {
            String path = ResourceUtils.getURL("classpath:").getPath();

            String src = path.substring(0, path.lastIndexOf("/"));

            StringBuffer cmd = new StringBuffer();

            cmd.append("cp -r ");

            if (files.length > 0) {
                for (String file : files){
                    cmd.append(src + origin + file + " ");
                }
                cmd.append(target);
            }else {
                cmd.append(src + origin).append(" ").append(target);
            }

            String[] copy = {"/bin/bash", "-c", cmd.toString()};

            Process process = Runtime.getRuntime().exec(copy);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;

            while ((line = br.readLine()) != null){
                sb.append(line).append("\n");
            }
            if (StringUtils.isBlank(sb.toString())) {
                isSuccess = true;
            }

        }catch (Exception e){
            logger.error("StorageServiceInterfaceImpl,copyFile,copy file error.", e);
        }finally {
            return isSuccess;
        }

    }

    /**
     * 将yaml字符串写入到新的文件中
     * @param file
     * @param path
     * @return
     */
    public boolean writeFile(String file, String path) {
        //标识是否写入文件成功
        boolean isSuccessed = false;
        //开始写入文件
        try {
            String dirs = path.substring(0, path.lastIndexOf("/"));
            //创建文件夹
            File dir = new File(dirs);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //创建文件
            File yaml = new File(path);
            if (!yaml.exists()) {
                yaml.createNewFile();
            }

            //写入文件
            byte[] bytes = new byte[1024];
            bytes = file.getBytes();
            int len = bytes.length;
            FileOutputStream fos = new FileOutputStream(yaml);
            fos.write(bytes, 0, len);
//            fos.write(bytes);
            fos.close();
            isSuccessed = true;
        }catch (IOException e){
            logger.error("YamlConfigUtils,writeYamlFile, write file error.", e);
        }finally {
            return isSuccessed;
        }
    }

    /**
     * 格式化字符串，替换掉其中的占位符
     * @param yaml
     * @param parames
     * @return
     */
    private String format(String yaml, String... parames) {
        //参数长度
        int len = parames.length;

        //替换占位符
        for (int i = 0 ; i < len; i ++) {
            yaml = yaml.replace("{" +i+"}", parames[i]);
        }
        return yaml;
    }

    /**
     * 设置overrides文件
     * @param overrides
     * @param entities
     * @return
     */
    public String addCephDevices(String overrides, List<HostEntity> entities) {
        String ip = entities.get(0).getIp();
        String network = ip.substring(0, ip.lastIndexOf(".")) + ".1";

        String[] devices = entities.get(0).getDevices();

        JSONObject jsonObject = JSON.parseObject(format(overrides,network));

        int i = 0;
        for (String device : devices) {
            String dev = device.replace("/","-");
            Map<String,Object> map = new HashMap<>();
            map.put("name", dev.substring(1));
            map.put("device",device);
            map.put("zap","1");
            JSONObject object =  new JSONObject(map);
            jsonObject.getJSONArray("osd_devices").fluentAdd(i ++,object);
        }
        return JSONObject.toJSONString(jsonObject,true);

    }
}

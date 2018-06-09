package io.xlauncher.storage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:20
 */
@Setter
@Getter
public class HostEntity {

    /**
     * 主机的IP地址
     */
    @JsonProperty(value = "ip")
    private String ip;

    /**
     * 主机的端口号
     */
    @JsonProperty(value = "port")
    private Integer port = 21;

    /**
     * 主机的访问用户名
     */
    @JsonProperty(value = "user")
    private String user = "root";

    /**
     * 主机访问的密码
     */
    @JsonProperty(value = "password")
    private String password;

    /**
     * 主机名称
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * 主机中作为存储初始化的磁盘
     */
    @JsonProperty(value = "devices")
    private String[] devices;

}

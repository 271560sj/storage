package io.xlauncher.storage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;
import java.util.Map;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-4 下午7:59
 * 存储卷的实体类
 */
@Setter
@Getter
public class VolumeEntity {

    /**
     * 存储卷的名称
     */
    @JsonProperty(value = "name")
    private String volumeName;

    /**
     * 存储卷的大小，单位是GB
     */
    @JsonProperty(value = "size")
    @Column(name = "volumeSize")
    private long volumeSize;

    /**
     * 存储卷创建以后，在存储服务系统在存储服务系统中保存时的id
     */
    @JsonProperty(value = "volumeId")
    private String volumeId;

    /**
     * 存储卷的类型,ext or xfs
     */
    @JsonProperty(value = "volumeType")
    private String volumeType;

    /**
     * 存储卷的创建时间
     */
    @JsonProperty(value = "createTime")
    private Date createTime;

    /**
     * 存储卷所属的用户的ID
     */
    @JsonProperty(value = "user")
    private String user;

    /**
     * 存储卷的使用状态
     */
    @JsonProperty(value = "volumeStatus")
    private String volumeStatus;

    /**
     * 存储卷的类型，replicate或者distribute,或者none
     */
    @JsonProperty(value = "durability")
    private Map<String,Object> durability;

}

package io.xlauncher.storage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
    @JsonProperty(value = "volumeName")
    private String volumeName;

    /**
     * 存储卷的大小，单位是GB
     */
    @JsonProperty(value = "volumeSize")
    private long volumeSize;

    /**
     * 存储卷的类型
     */
    @JsonProperty(value = "volumeType")
    private String volumeType;

    /**
     * 存储卷的创建时间
     */
    @JsonProperty(value = "createTime")
    private String createTime;

    /**
     * 存储卷所属的用户的ID
     */
    @JsonProperty(value = "userId")
    private String userId;

    /**
     * 存储卷的使用状态
     */
    @JsonProperty(value = "volumeStatus")
    private String volumeStatus;
}

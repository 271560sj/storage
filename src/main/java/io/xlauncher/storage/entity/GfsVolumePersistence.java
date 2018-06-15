package io.xlauncher.storage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-14 下午1:43
 */
@Setter
@Getter
@Entity
@Table(name = "gfsvolume")
@EntityListeners(AuditingEntityListener.class)
public class GfsVolumePersistence implements Serializable{

    /**
     * gfs数据表的ID
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(value = "id")
    private Long id;

    /**
     * 存储卷的名称
     */
    @Column(name = "volumeName")
    @JsonProperty(value = "name")
    private String volumeName;

    /**
     * 存储卷的id
     */
    @Column(name = "volumeId")
    @JsonProperty(value = "volumeId")
    private String volumeId;

    /**
     * 存储卷的大小
     */
    @Column(name = "volumeSize")
    @JsonProperty(value = "size")
    private String volumeSize;

    /**
     * 存储卷的类型
     */
    @Column(name = "volumeType")
    @JsonProperty(value = "volumeType")
    private String volumeType;

    /**
     * 存储卷的创建时间
     */
    @Column(name = "createTime")
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(value = "createTime")
    private Date createTime;

    /**
     * 存储卷的状态
     */
    @Column(name = "volumeStatus")
    @JsonProperty(value = "volumeStatus")
    private String volumeStatus;

    /**
     * 用户名称
     */
    @Column(name = "userName")
    @JsonProperty(value = "user")
    private String userName;

    /**
     * 用户id
     */
    @Column(name = "userId")
    @JsonProperty(value = "userId")
    private int userId;
}

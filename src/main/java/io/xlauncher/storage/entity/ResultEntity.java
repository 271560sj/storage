package io.xlauncher.storage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-7 上午10:13
 */
@Setter
@Getter
public class ResultEntity {

    /**
     * 返回结果的code
     */
    @JsonProperty(value = "code")
    private int code;

    /**
     * 返回结果的消息
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * 返回结果的消息体
     */
    @JsonProperty(value = "object")
    private Object object;
}

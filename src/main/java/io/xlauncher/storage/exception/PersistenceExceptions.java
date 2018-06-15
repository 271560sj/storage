package io.xlauncher.storage.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @Author sujin@xlauncher.io
 * @Date 18-6-14 下午2:59
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Setter
@Getter
public class PersistenceExceptions extends RuntimeException{

    private String resource;

    private String filed;

    private Object value;

    public PersistenceExceptions(String resource, String filed, Object value){
        super(String.format("%s not found with %s : '%s'",resource, filed, value));
    }

}

package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;
import lombok.Getter;

/**
 * 自定义异常
 */
@Data
public class LyException extends RuntimeException{

    //状态码
    private Integer status;

    public LyException(Integer status,String message ) {
        super(message);
        this.status = status;
    }

    public LyException(Integer status,String message, Throwable cause ) {
        super(message, cause);
        this.status = status;
    }

    public LyException(ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }
}

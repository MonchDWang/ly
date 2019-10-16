package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BasicExceptionAdvice {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleLyException(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
}

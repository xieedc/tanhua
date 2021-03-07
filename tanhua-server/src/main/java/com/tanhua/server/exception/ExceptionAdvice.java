package com.tanhua.server.exception;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一异常处理类
 * RestControllerAdvice：拦截业务层异常
 * @ExceptionHandler：处理异常注解
 * @ExceptionHandler(TanHuaException.class)：定义的处理哪个异常
 */
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 处理自定义的业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(TanHuaException.class)
    public ResponseEntity handleTanHuaException(TanHuaException ex){
        if(null != ex.getErrData()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getErrData());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResult.error("000009",ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception ex){
        log.error("发生未知异常",ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResult.error());
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        log.info("222****beforeBodyWrite**返回数据**"+o.toString());
        return o;
    }
}
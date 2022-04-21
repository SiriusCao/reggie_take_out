package com.cao.reggie.common;

/**
 * 自定义业务异常类，继承自RuntimeException
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}

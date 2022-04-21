package com.cao.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 对用户名重复产生的异常进行处理
     *
     * @param exception Duplicate entry
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        //获取异常信息
        String message = exception.getMessage();
        log.error(message);
        //判断是不是重复插入导致的异常
        if (message.contains("Duplicate entry")) {
            return R.error(message.split(" ")[2] + "已存在");
        }
        return R.error("未知错误");
    }
}

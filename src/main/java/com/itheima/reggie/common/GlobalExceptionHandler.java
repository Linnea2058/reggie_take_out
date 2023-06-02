package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;


//该通知拦截包含annotations注解的类
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
//将结果转成json格式返回
@ResponseBody
public class GlobalExceptionHandler {
    //表明是异常处理器
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){//参数表示拦截数据库返回的错误
        //查看是否是用户名已存在错误
        if(ex.getMessage().contains("Duplicate entry")){//查看msg中是否存在"Duplicate entry"信息，
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在";
            return R.error(msg);
        }
        return R.error("出错了");
    }
}

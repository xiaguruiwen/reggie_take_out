package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 * @author swh
 * @creat 2022/7/29
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}

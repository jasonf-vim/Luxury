package com.jasonf.entity;

import java.io.Serializable;

/**
 * 返回码
 */
public class StatusCode implements Serializable {

    private static final long serialVersionUID = -6199712919690130627L;

    public static final int OK = 20000; //成功
    public static final int ERROR = 20001;  //失败
    public static final int LOGIN_ERROR = 20002;    //用户名或密码错误
    public static final int ACCESS_ERROR = 20003;   //权限不足
    public static final int REMOTE_ERROR = 20004;   //远程调用失败
    public static final int REP_ERROR = 20005;  //重复操作

}

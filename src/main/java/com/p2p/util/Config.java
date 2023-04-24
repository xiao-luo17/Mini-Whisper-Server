package com.p2p.util;

public class Config {
    //整个项目的静态配置类

    //请求代码，标识请求方需要的数据类型
    /**
     * 注册请求
     */
    public static final int REGISTER_EXIT = 1;
    /**
     * 获取用户列表请求
     */
    public static final int GET_REGISTER_MAP = 2;
    /**
     * 获取聊天对象IP地址请求
     */
    public static final int GET_OTHER_ADDRESS = 3;
    /**
     * 登出请求
     */
    public static final int EXIT = 4;
    /**
     * 登录请求
     */
    public static final int SIGN_IN = 5;

    //响应代码，标识接收方需要准备的响应类型
    /**
     * String响应
     */
    public static final int STRING_TYPE = 1;
    /**
     * Vector响应
     */
    public static final int VECTOR_TYPE = 2;
    /**
     * IP地址类响应
     */
    public static final int IP_ADDRESS_TYPE = 3;
    /**
     * String退出响应
     */
    public static final int LOGOUT = 4;
}

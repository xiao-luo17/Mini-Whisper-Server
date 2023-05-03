package com.p2p.util;

import com.p2p.Server.ServerThread;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Vector;

public class Config {
    //整个项目的静态配置类

    /**
     * 消息转发公共区域
     */
    public static volatile String relayMessage = "";
    public static volatile String relayToThreadName = "";
    public static volatile String relayFromThreadName = "";

    /**
     * 这里是服务器启动线程总表
     */
    public static Vector<ServerThread> serverThreadVector = new Vector<>();
    /**
     * 这里是用户名到ip的映射，同时也是在线用户名册
     */
    public static Hashtable<String, InetSocketAddress> registerMap = new Hashtable<>();
    /**
     * 这里是用户名到密码的映射，同时是所有用户名册
     */
    public static Hashtable<String, String> registerPassword = new Hashtable<>();

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
    /**
     * 服务端转发请求
     */
    public static final int CHAT_RELAY = 6;

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
     * 消息转发响应
     */
    public static final int RELAY_MESSAGE_TYPE = 4;
}

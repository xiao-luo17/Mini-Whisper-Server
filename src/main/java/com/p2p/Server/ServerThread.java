package com.p2p.Server;

import com.p2p.util.Request;
import com.p2p.util.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import static com.p2p.util.Config.*;

public class ServerThread implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Thread serverChild;
    /**
     * 这里是用户名到ip的映射，同时也是在线用户名册
     */
    private static Hashtable<String, InetSocketAddress> registerMap = new Hashtable<>();
    /**
     * 这里是用户名到密码的映射，同时是所有用户名册
     */
    public static Hashtable<String, String> registerPassword = new Hashtable<>();
    private Request request;
    private Response response;
    private boolean keepListening = true;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public synchronized void start() {
        if (serverChild == null) {
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                serverChild = new Thread(this);
                serverChild.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        if (serverChild != null) {
            try {
                serverChild.interrupt();
                serverChild = null;
                ois.close();
                oos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            while (keepListening) {
                receiveRequest();
                parseRequest();
                sendResponse();
            }
            stop();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            stop();
            System.err.println("[系统消息] 客户端登录异常... 客户端中断连接...");
        }
    }

    private void receiveRequest() throws IOException, ClassNotFoundException {
        request = (Request) ois.readObject();
    }

    private void parseRequest() {
        if (request == null)
            return;
        response = null;
        int requestType = request.getRequestType();
        String registerName = request.getRegisterName();
        if (requestType != 1 && !isRegister(registerName)) {
            response = new Response(1, registerName + "你还未注册");
            return;
        }
        switch (requestType) {//测试请求类型
            case REGISTER_EXIT:
                if (isRegister(registerName)) {
                    response = new Response(STRING_TYPE, "|" + registerName + "|" + "已被其他人使用，请使用其他名字注册");
                    break;
                }
                //这里是注册map表
                registerPassword.put(registerName, request.getPassword());
                response = new Response(STRING_TYPE, registerName + ",你已经注册成功");
                System.out.println("[系统消息] |" + registerName + "| 注册成功...");
                break;
            case SIGN_IN:
                if(registerMap.containsKey(registerName)){
                    response = new Response(STRING_TYPE, registerName + ",该账号已经在线");
                    System.out.println("[系统消息] |" + registerName + "| 该在线账号试图重复登录...");
                    break;
                }
                if (request.getPassword().equals(registerPassword.get(registerName))) {
                    registerMap.put(registerName, new InetSocketAddress(socket.getInetAddress(), request.getUDPPort()));
                    response = new Response(STRING_TYPE, registerName + ",你已经登录成功");
                    System.out.println("[系统消息] |" + registerName + "| 登录成功...");
                } else {
                    response = new Response(STRING_TYPE, registerName + ",你的密码错误");
                    System.out.println("[系统消息] |" + registerName + "| 登录密码错误...");
                    break;
                }
                break;
            case GET_REGISTER_MAP:
                Vector<String> registerList = new Vector<>();
                for (String key : registerMap.keySet()) {
                    registerList.addElement(key);
                }
                //这里是已经注册的用户
                Vector<String> registerAll = new Vector<>();
                for (String key : registerPassword.keySet()) {
                    registerAll.addElement(key);
                }
                response = new Response(VECTOR_TYPE, registerList, registerAll);
                System.out.println("[系统消息] |" + registerName + "| 成功请求用户列表...");
                break;
            case GET_OTHER_ADDRESS:
                String chatRegisterName = request.getChatRegisterName();
                InetSocketAddress chatP2PEndAddress = registerMap.get(chatRegisterName);
                response = new Response(IP_ADDRESS_TYPE, chatP2PEndAddress);
                System.out.println("[系统消息] |" + registerName + "| 请求 |" + chatRegisterName + "| 的IP和UDP端口号");
                break;
            case EXIT:
                registerMap.remove(registerName);
                response = new Response(STRING_TYPE, registerName + ",你已经从服务器退出！");
                keepListening = false;
                System.out.println("[系统消息] |" + registerName + "| 从在线列表退出...");
        }
    }

    /**
     * 检查name是否已经被注册，true为已经被注册
     */
    private boolean isRegister(String name) {
        return name != null && ServerThread.registerPassword.get(name) != null;
    }

    /**
     * 向output输出流发送返回消息
     */
    private void sendResponse() throws IOException {
        if (response != null) {
            oos.writeObject(response);
        }
    }

}

package com.p2p.Server;

import com.p2p.util.Request;
import com.p2p.util.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import static com.p2p.util.Config.*;

public class ServerThread implements Runnable {

    private String threadName;
    private int threadType;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Thread serverChild;

    private Request request;
    private Response response;
    private boolean keepListening = true;
    private boolean keepSending = true;

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
        if(threadType == 1){
            for (ServerThread serverThread : serverThreadVector) {
                if (serverThread.threadName.equals("Sending" + threadName.substring(9))) {
                    serverThread.stop();
                }
            }
        }
        if (serverChild != null) {
            try {
                System.out.println("[系统消息 线程--" + threadName + "] " + "线程已退出，socket关闭");
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
            //先进行线程类型判断
            request = (Request) ois.readObject();
            if (request.getThreadType() == 1) {
                listeningThread();
            }
            if (request.getThreadType() == 2) {
                sendingThread();
            }
        } catch (IOException | ClassNotFoundException e) {
//            System.err.println("[系统消息 线程--" + threadName + "] " + "对接用户已经退出");
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
            response = new Response(STRING_TYPE, registerName + "你还未注册");
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
                System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 注册成功...");
                break;
            case SIGN_IN:
                if (registerMap.containsKey(registerName)) {
                    response = new Response(STRING_TYPE, registerName + ",该账号已经在线");
                    System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 该在线账号试图重复登录...");
                    break;
                }
                if (request.getPassword().equals(registerPassword.get(registerName))) {
                    registerMap.put(registerName, new InetSocketAddress(socket.getInetAddress(), request.getUDPPort()));
                    response = new Response(STRING_TYPE, registerName + ",你已经登录成功");
                    System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 登录成功...");
                } else {
                    response = new Response(STRING_TYPE, registerName + ",你的密码错误");
                    System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 登录密码错误...");
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
                System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 成功请求用户列表...");
                break;
            case GET_OTHER_ADDRESS:
                String chatRegisterName = request.getChatRegisterName();
                InetSocketAddress chatP2PEndAddress = registerMap.get(chatRegisterName);
                response = new Response(IP_ADDRESS_TYPE, chatP2PEndAddress);
                System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 请求 |" + chatRegisterName + "| 的IP和UDP端口号");
                break;
            case CHAT_RELAY:
                //消息加入到公共区域，线程循环搜索是否有自己的消息
                relayMessage = request.getRelayMessage();
                relayToThreadName = "Sending" + request.getChatRegisterName();
                relayFromThreadName = threadName;
                response = new Response(STRING_TYPE, "正在尝试服务器转发");
                System.out.println("[系统消息 线程--" + threadName + "] |准备| 转发 |" + registerName + "| 的消息到 |" + relayToThreadName + "|");
                break;
            case EXIT:
                registerMap.remove(registerName);
                response = new Response(STRING_TYPE, registerName + ",你已经从服务器退出！");
                keepListening = false;
                System.out.println("[系统消息 线程--" + threadName + "] |" + registerName + "| 从在线列表退出...");
        }
    }

    private void sendingThread() {
        try {
            threadName = "Sending" + request.getRegisterName();
            threadType = 2;
            serverThreadVector.add(this);
            response = new Response(STRING_TYPE, "开启服务器sending类型线程");
            System.out.println("[系统消息 线程--" + threadName + "] 启动" + threadName + "收线程");
            sendResponse();
            while (keepSending) {
                if (relayToThreadName.equals(threadName)) {
                    response = new Response(RELAY_MESSAGE_TYPE, registerMap.get(relayFromThreadName.substring(9)), relayFromThreadName.substring(9), relayMessage);
                    sendResponse();
                    System.out.println("[系统消息 线程--" + threadName + "] |成功| 转发 |" + relayFromThreadName.substring(9) + "| 的消息到 |" + relayToThreadName.substring(7) + "|");
                    relayToThreadName = "";
                    relayMessage = "";
                }
            }
        } catch (IOException e) {
            stop();
            System.err.println("[系统消息 线程--" + threadName + "] " + "对接用户已经退出");
        }
    }

    private void listeningThread() {
        try {
            threadName = "Listening" + request.getRegisterName();
            threadType = 1;
            serverThreadVector.add(this);
            response = new Response(STRING_TYPE, "开启服务器Listening类型线程");
            System.out.println("[系统消息 线程--" + threadName + "] 启动" + threadName + "收线程");
            sendResponse();
            while (keepListening) {
                receiveRequest();
                parseRequest();
                sendResponse();
            }
            stop();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            registerMap.remove(threadName.substring(9));
            stop();
            System.err.println("[系统消息 线程--" + threadName + "] " + "对接用户已经退出");
        }
    }

    /**
     * 检查name是否已经被注册，true为已经被注册
     */
    private boolean isRegister(String name) {
        return name != null && registerPassword.get(name) != null;
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

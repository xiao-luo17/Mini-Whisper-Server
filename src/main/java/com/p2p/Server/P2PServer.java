package com.p2p.Server;

import java.net.ServerSocket;
import java.net.Socket;

import static com.p2p.Server.ServerThread.registerPassword;
import static com.p2p.util.DataSave.MapToTxT;
import static com.p2p.util.DataSave.TxTToMap;

public class P2PServer {
    public static final int PORT = 8000;
    public static final int MAX_QUEUE_LENGTH = 100;

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT, MAX_QUEUE_LENGTH);
            System.out.println("*******服务器已经启动*******");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[系统消息] 已接收到客户：" + socket.getInetAddress());
                ServerThread serverThread = new ServerThread(socket);

                serverThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //进程结束进行数据存储
    private void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MapToTxT(registerPassword);
        }));
    }

    public static void main(String[] args) {
        P2PServer ms = new P2PServer();
        ms.doShutDownWork();
        //进行数据导入
        registerPassword = TxTToMap();
        ms.start();
    }
}

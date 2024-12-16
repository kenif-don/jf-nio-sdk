package com.jf.im;

import com.jf.im.model.ChannelConst;
import com.jf.im.model.DataProtoType;
import com.jf.im.process.LogicProcess;
import com.jf.im.server.TCPServer;
import com.jf.im.server.UDPServer;
import com.jf.im.server.WSServer;

public class IMServer {
    private static IMServer server = new IMServer();
    public IMServer initLogicProcess(LogicProcess logicProcess){
        ChannelConst.LOGIC_PROCESS = logicProcess;
        return server;
    }
    private IMServer(){
    }
    public static IMServer getInstance(){
        return server;
    }

    /**
     * 启动TCP协议 并绑定数据协议
     * */
    public void startupTCP(int port,DataProtoType dataProtoType) throws InterruptedException {
        //初始化tcp协议/消息协议类型
        TCPServer tcpServer = TCPServer.getInstance();
        tcpServer.startup(port,dataProtoType);
    }
    public void startupUDP(int port,DataProtoType dataProtoType) throws InterruptedException {
        UDPServer udpServer = UDPServer.getInstance();
        udpServer.startup(port,dataProtoType);
    }
    public void startupWS(int port,DataProtoType dataProtoType) throws InterruptedException {
        WSServer wsServer = WSServer.getInstance();
        wsServer.startup(port,dataProtoType);
    }

    /**
     * 退出
     */
    public void shutdown(){
        TCPServer.getInstance().close();
        UDPServer.getInstance().close();
        WSServer.getInstance().close();
    }
}

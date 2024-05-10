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
    public IMServer initDataProtoType(DataProtoType dataProtoType){
        ChannelConst.DATA_PROTO_TYPE = dataProtoType;
        return server;
    }
    private IMServer(){
    }
    public static IMServer getInstance(){
        return server;
    }
    //启动三种协议
    public void startupAll(int tcp_port,int udp_port,int ws_port){
        startupTCP(tcp_port);
        startupUDP(udp_port);
        startupWS(ws_port);
    }
    public void startupTCP(int port){
        try {
            //初始化tcp协议/消息协议类型
            TCPServer tcpServer = TCPServer.getInstance();
            tcpServer.startup(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startupUDP(int port){
        try {
            UDPServer udpServer = UDPServer.getInstance();
            udpServer.startup(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startupWS(int port){
        try {
            WSServer wsServer = WSServer.getInstance();
            wsServer.startup(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

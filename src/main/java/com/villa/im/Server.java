package com.villa.im;

import com.villa.im.handler.CoreHandler;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.DataProtoType;
import com.villa.im.process.LogicProcess;
import com.villa.im.server.TCPServer;
import com.villa.im.server.UDPServer;
import com.villa.im.server.WSServer;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Server {
    private static Server server = new Server();
    public Server initLogicProcess(LogicProcess logicProcess){
        ChannelConst.LOGIC_PROCESS = logicProcess;
        return server;
    }
    public Server initDataProtoType(DataProtoType dataProtoType){
        ChannelConst.DATA_PROTO_TYPE = dataProtoType;
        return server;
    }
    private Server(){
    }
    public static Server getInstance(){
        return server;
    }
    //启动三种协议
    public void startupAll(int tcp_port,int udp_port,int ws_port){
        startupTCP(tcp_port);
        startupTCP(udp_port);
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
}

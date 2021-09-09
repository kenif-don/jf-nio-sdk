package com.villa.im;

import com.villa.im.handler.CoreHandler;
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
    //核心处理器 同样是单例
    private CoreHandler coreHandler;
    //逻辑业务器 当前框架只负责接口 具体实现需要使用者实现LogicProcess接口
    private LogicProcess logicProcess;

    public void initLogicProcess(LogicProcess logicProcess){
        this.logicProcess = logicProcess;
    }
    private Server(){
        this.coreHandler = new CoreHandler();
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
            TCPServer.getInstance(coreHandler).startup(4002);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startupUDP(int port){
        try {
            UDPServer.getInstance(coreHandler).startup(4003);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startupWS(int port){
        try {
            WSServer.getInstance(coreHandler).startup(4001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package com.jf.im.server;

import com.jf.im.handler.CoreHandler;
import com.jf.im.handler.WebSocketHandler;
import com.jf.im.model.ProtoType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * netty-ws服务器 tcp/udp/ws(wss) 三种协议可以同时存在
 */
public class WSServer extends BaseServer{
    //饿汉单例
    private static WSServer server = new WSServer();
    private WSServer(){
        setProtoType(ProtoType.WS);
        //如果是wss协议 需要在这里加载证书
    }
    public static WSServer getInstance(){
        return server;
    }
    protected void init(){
        super.init();
        //异步的服务器端 UDP Socket 连接
        getBootstrap().channel(NioServerSocketChannel.class);
    }
    protected void initChildChannelHandler() {
        ((ServerBootstrap)getBootstrap()).childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel channel) {
                //编解码器
                channel.pipeline()
                .addLast(new IdleStateHandler(30,30,30))
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(1024*1024*10))
                .addLast(new WebSocketServerProtocolHandler("/",null,false,1024*1024*10,false))
                //装载核心处理器
                .addLast(new WebSocketHandler(CoreHandler.newInstance()));
            }
        });
    }
}
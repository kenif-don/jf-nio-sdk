package com.villa.im.server;

import com.alibaba.fastjson.JSON;
import com.villa.im.handler.CoreHandler;
import com.villa.im.handler.UDPHandler;
import com.villa.im.model.ProtoType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * netty-udp服务器 tcp/udp/ws(wss) 三种协议可以同时存在
 * UDP与TCP的区别在于 UDP不用区分客户端和服务器,交互消息时也不用保持链接。
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class UDPServer extends BaseServer{
    //饿汉单例
    private static UDPServer server = new UDPServer();
    private UDPServer(){
        setProtoType(ProtoType.UDP);
    }
    public static UDPServer getInstance(){
        return server;
    }
    protected void init(){
        super.init();
        //开启广播
        ((Bootstrap)getBootstrap()).option(ChannelOption.SO_BROADCAST,true);
        //异步的服务器端 UDP Socket 连接
        getBootstrap().channel(NioDatagramChannel.class).handler(new LoggingHandler(LogLevel.ERROR));

    }
    protected void initChildChannelHandler() {
        ((Bootstrap)getBootstrap()).handler(new ChannelInitializer<NioDatagramChannel>() {
            protected void initChannel(NioDatagramChannel  channel) {
                //jSON解码器
                channel.pipeline()
                .addLast(new IdleStateHandler(30,0,0))
                .addLast(new JsonObjectDecoder())
                //JSON编码器
                .addLast(new MessageToByteEncoder<Object>() {
                    protected void encode(ChannelHandlerContext channel, Object in, ByteBuf out) throws Exception {
                        out.writeBytes(JSON.toJSONBytes(in));
                    }
                })
                //装载核心处理器
                .addLast(new UDPHandler(CoreHandler.newInstance()));
            }
        });
    }
}
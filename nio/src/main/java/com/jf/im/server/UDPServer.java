package com.jf.im.server;

import com.alibaba.fastjson2.JSON;
import com.jf.im.handler.CoreHandler;
import com.jf.im.handler.UDPHandler;
import com.jf.im.model.ChannelConst;
import com.jf.im.model.ProtoBuf;
import com.jf.im.model.ProtoType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * netty-udp服务器 tcp/udp/ws(wss) 三种协议可以同时存在
 * UDP与TCP的区别在于 UDP不用区分客户端和服务器,交互消息时也不用保持链接。
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
        getBootstrap().channel(NioDatagramChannel.class);

    }
    protected void initChildChannelHandler() {
        ((Bootstrap)getBootstrap()).handler(new ChannelInitializer<NioDatagramChannel>() {
            protected void initChannel(NioDatagramChannel  channel) {
                //jSON解码器
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new IdleStateHandler(30,30,30));
                pipeline.addLast(new JsonObjectDecoder());
                //JSON编码器
                switch (ChannelConst.DATA_PROTO_TYPE){
                    case JSON://如果使用json协议的编解码器
                        //JSON编码器--客户端以JSON字符串方式接收
                        pipeline.addLast(new MessageToByteEncoder<Object>() {
                            protected void encode(ChannelHandlerContext channel, Object in, ByteBuf out) throws Exception {
                                out.writeBytes(JSON.toJSONBytes(in));
                            }
                        });
                        break;
                    case PROTOBUF://如果使用protobuf的编解码器
                        pipeline.addLast(new MessageToByteEncoder<ProtoBuf.proto_my>() {
                            //编码器
                            protected void encode(ChannelHandlerContext channelHandlerContext, ProtoBuf.proto_my in, ByteBuf out) throws Exception {
                                out.writeBytes(in.toByteArray());
                            }
                        });
                        break;
                }
                //装载核心处理器
                pipeline.addLast(new UDPHandler(CoreHandler.newInstance()));
            }
        });
    }
}
package com.villa.im.server;

import com.alibaba.fastjson.JSON;
import com.villa.im.handler.CoreHandler;
import com.villa.im.handler.TCPHandler;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoBuf;
import com.villa.im.model.ProtoType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * netty-tcp服务器 tcp/udp/ws(wss) 三种协议可以同时存在
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class TCPServer extends BaseServer{
    //饿汉单例
    private static TCPServer server = new TCPServer();
    private TCPServer(){
        //设置协议类型
        setProtoType(ProtoType.TCP);
    }
    public static TCPServer getInstance(){
        return server;
    }
    public void init() {
        super.init();
        //child channel 禁用nagle算法  nagle算法：tcp内置缓冲区,这个缓存区需要被写满才能进行发送。对于数据量太小的交互，会出现高延迟，所以需要关闭
        ((ServerBootstrap)getBootstrap()).childOption(ChannelOption.TCP_NODELAY, true);
        //child channel 开启心跳
        ((ServerBootstrap)getBootstrap()).childOption(ChannelOption.SO_KEEPALIVE, true);
        //异步的服务器端 TCP Socket 连接
        getBootstrap().channel(NioServerSocketChannel.class);
        /**
         *  服务器端TCP内核模块维护有2个队列，我们称之为A，B吧
         *  客户端向服务端connect的时候，发送带有SYN标志的包（第一次握手）
         *  服务端收到客户端发来的SYN时，向客户端发送SYN ACK 确认(第二次握手)
         *  此时TCP内核模块把客户端连接加入到A队列中，然后服务器收到客户端发来的ACK时（第三次握手）
         *  TCP没和模块把客户端连接从A队列移到B队列，连接完成，应用程序的accept会返回
         *  也就是说accept从B队列中取出完成三次握手的连接
         *  A队列和B队列的长度之和是backlog,当A，B队列的长之和大于backlog时，新连接将会被TCP内核拒绝
         *  所以，如果backlog过小，可能会出现accept速度跟不上，A.B 队列满了，导致新客户端无法连接，
         *  要注意的是，backlog对程序支持的连接数并无影响，backlog影响的只是还没有被accept 取出的连接
         */
        getBootstrap().option(ChannelOption.SO_BACKLOG, 4096);
    }
    @Override
    protected void initChildChannelHandler() {
        ((ServerBootstrap)getBootstrap()).childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel  channel) {
                ChannelPipeline pipeline = channel.pipeline();
                //30秒客户端和服务器未交互,则触发超时事件--需要放在解码器前面才能生效
                pipeline.addLast(new ReadTimeoutHandler(30));
                //通过将消息分为消息头和消息体来处理沾包半包问题
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024*1024*10+4, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
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
                pipeline.addLast(new TCPHandler(CoreHandler.newInstance()));
            }
        });
    }
}
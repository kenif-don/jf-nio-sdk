package com.jf.im.server;

import com.alibaba.fastjson2.JSON;
import com.jf.im.model.DataProtoType;
import com.jf.im.model.ProtoBuf;
import com.jf.im.model.ProtoType;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**TCP/UDP/WS的抽象父类 将一些公共属性和方法进行抽取*/
public abstract class BaseServer {
    //协议类型
    @Getter@Setter
    private ProtoType protoType;
    //协议服务器启动状态
    @Getter@Setter
    private volatile boolean isRunning = false;
    //netty的核心工厂  引导器
    private AbstractBootstrap bootstrap;
    //boss线程组 类似包工头 监听parent channel管理child channel的
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    //work线程 搬砖的 每个客户端对应一个channel 一个channel一个线程
    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    //服务端
    protected Channel serverChannel;
    protected void init(DataProtoType dataProtoType){
        //设置reactor线程
        if (Objects.requireNonNull(protoType) == ProtoType.UDP) {
            getBootstrap().group(workGroup);
        } else {
            ((ServerBootstrap) getBootstrap()).group(bossGroup, workGroup);
        }
        //流水线装配
        initChildChannelHandler();
    }

    /**
     * 通道关闭监听
     */
    protected void addCloseListener(ChannelFuture cf){
        cf.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> {
            //服务器停止标志
            isRunning = false;
            //释放所有资源及关闭boss/work线程组
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        });
    }
    /**
     * 服务器启动
     */
    public void startup(int port, DataProtoType dataProtoType) throws InterruptedException{
        if(isRunning())return;
        //初始化
        init(dataProtoType);
        //同步阻塞  知道绑定成功
        ChannelFuture cf = getBootstrap().bind(port).sync();
        serverChannel = cf.channel();
        //如果绑定失败 直接退出 不进入后续流程
        if(!cf.isSuccess())return;
        //服务启动成功标志
        setRunning(true);
        //添加监听
        addCloseListener(cf);
        serverChannel.closeFuture().addListener((ChannelFutureListener) future -> {
            //监听到服务端通道关闭时释放线程组
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        });
    }

    /**
     * 停止服务器
     */
    public void close(){
        if(isRunning()&&serverChannel!=null){
            serverChannel.close();
        }
    }
    /**
     * 不同协议需要不同的处理器  所以交给子类实现
     */
    protected abstract void initChildChannelHandler();

    public AbstractBootstrap getBootstrap() {
        if(bootstrap!=null)return bootstrap;
        switch (protoType){
            case WS:
            case TCP:
                bootstrap = new ServerBootstrap();
                break;
            case UDP:
                bootstrap = new Bootstrap();
                break;
        }
        return bootstrap;
    }
    protected void initEncoder(ChannelPipeline pipeline, DataProtoType dataProtoType) {
        switch (dataProtoType){
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
    }
}

package com.villa.im.server;

import com.villa.im.handler.CoreHandler;
import com.villa.im.model.ProtoType;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * TCP/UDP/WS的抽象父类 将一些公共属性和方法进行抽取
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public abstract class BaseServer {
    //协议类型
    private ProtoType protoType;
    //协议服务器启动状态
    private volatile boolean isRunning = false;
    //netty的核心工厂  引导器
    private AbstractBootstrap bootstrap;
    //boss线程组 类似包工头 监听parent channel管理child channel的
    protected final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    //work线程 搬砖的 监听child channel 每个客户端对应一个channel 一个channel一个线程
    protected final EventLoopGroup workGroup = new NioEventLoopGroup();
    //服务端
    protected Channel serverChannel;
    protected void init(){
        //设置reactor线程
        switch (protoType){
            case UDP:
                getBootstrap().group(workGroup);
                break;
            default:
                ((ServerBootstrap)getBootstrap()).group(bossGroup,workGroup);
                break;
        }
        //流水线装配
        initChildChannelHandler();
    }

    /**
     * 通道关闭监听
     */
    protected void addCloseListener(ChannelFuture cf){
        cf.channel().closeFuture().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //服务器停止标志
                isRunning = false;
                //释放所有资源及关闭boss/work线程组
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        });
    }
    /**
     * 服务器启动
     */
    public void startup(int port) throws InterruptedException{
        if(isRunning())return;
        //初始化
        init();
        //同步阻塞  知道绑定成功
        ChannelFuture cf = getBootstrap().bind(port).sync();
        serverChannel = cf.channel();
        //如果绑定失败 直接退出 不进入后续流程
        if(!cf.isSuccess())return;
        //服务启动成功标志
        setRunning(true);
        //添加监听
        addCloseListener(cf);
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

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

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

    public ProtoType getProtoType() {
        return protoType;
    }

    public void setProtoType(ProtoType protoType) {
        this.protoType = protoType;
    }
}

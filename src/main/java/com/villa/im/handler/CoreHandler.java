package com.villa.im.handler;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.Protocol;
import com.villa.im.protocol.ProtocolAction;
import com.villa.im.util.Log;
import com.villa.im.util.Util;
import io.netty.channel.ChannelHandlerContext;

/**
 * TCP/UDP/WS 统一的处理器
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class CoreHandler {
    private static int channelCount = 0;
    private static CoreHandler coreHandler = new CoreHandler();
    private CoreHandler(){

    }
    public static CoreHandler newInstance(){
        return coreHandler;
    }
    public void channelRead0(ChannelHandlerContext ctx, Protocol protocol) {
        Log.log("接收到客户端消息:"+JSON.toJSONString(protocol));
        switch (protocol.getType()){
            //客户端登录
            case ChannelConst.CHANNEL_LOGIN:
                //获取连接标识符
                String channelId = protocol.getFrom();
                if(!Util.isNotEmpty(channelId)){
                    //发送消息给客户端,需要连接标识符
                    ProtocolAction.sendMsg(ctx.channel(), "401",ChannelConst.CHANNEL_LOGIN);
                    return;
                }
                //将连接标识符存入连接属性中
                Util.putChannelId(ctx.channel(), channelId);
                //将连接保存
                ChannelHandler.getInstance().addChannel(ctx.channel());
                //发送请求结果给客户端

                ProtocolAction.sendMsg(ctx.channel(), "200",ChannelConst.CHANNEL_LOGIN);
                break;
            //客户端退出登录
            case ChannelConst.CHANNEL_LOGOUT:
                //踢掉客户端
                ChannelHandler.getInstance().kickChannel(ctx.channel());
                break;
            //心跳应答
            case ChannelConst.CHANNEL_HEART:
                ProtocolAction.sendOkACK(ctx.channel(),ChannelConst.CHANNEL_HEART);
                break;
            //客户端发送消息
            case ChannelConst.CHANNEL_MSG:
                ProtocolAction.sendMsg(ctx.channel(),protocol,ChannelConst.LOGIC_PROCESS);
                break;
            case ChannelConst.CHANNEL_ACK:
                ProtocolAction.ack(protocol);
                break;
        }
    }
    /**
     * 当有新的Channel连接 时候会触发   
     */
    public void handlerAdded(ChannelHandlerContext ctx) {
        Log.log("新连接进入,当前连接数："+ ++channelCount);
    }

    /**
     * 连接断开的时候触发
     */
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String channelId = Util.getChannelId(ctx.channel());
        //登录过才踢 否则不做任何处理，因为本身也没有进行保存
        if(Util.isNotEmpty(channelId)){
            ChannelHandler.getInstance().kickChannel(ctx.channel());
        }
        Log.log("有连接断开,当前连接数:"+--channelCount);
    }

    /**
     * 出现异常时候触发,关闭当前连接
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String channelId = Util.getChannelId(ctx.channel());
        if(Util.isNotEmpty(channelId)){
            throw new RuntimeException(String.format("[%s]连接发生异常：%s",channelId,cause.getMessage()));
        }else{
            throw new RuntimeException(String.format("未登录的连接发生异常：%s",cause.getMessage()));
        }
    }
}

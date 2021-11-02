package com.villa.im.handler;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.Protocol;
import com.villa.im.manager.ProtocolManager;
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
        switch (protocol.getType()){
            //客户端登录
            case ChannelConst.CHANNEL_LOGIN:
                if(!ChannelConst.LOGIC_PROCESS.loginBefore(ctx.channel(), protocol)){
                    return;
                }
                //获取连接标识符
                String channelId = protocol.getFrom();
                if(!Util.isNotEmpty(channelId)){
                    //发送消息给客户端,需要连接标识符
                    ProtocolManager.sendAck(ctx.channel(), ChannelConst.NOT_LOGIN_ID,ChannelConst.CHANNEL_LOGIN);
                    return;
                }
                //将连接标识符存入连接属性中
                Util.putChannelId(ctx.channel(), channelId);
                //将连接保存
                ChannelHandler.getInstance().addChannel(ctx.channel());
                //发送请求结果给客户端
                ProtocolManager.sendAck(ctx.channel(), ChannelConst.SUCCESS,ChannelConst.CHANNEL_LOGIN);
                break;
            //客户端退出登录
            case ChannelConst.CHANNEL_LOGOUT:
                if(!ChannelConst.LOGIC_PROCESS.logoutBefore(ctx.channel(), protocol)){
                    return;
                }
                //踢掉客户端
                ChannelHandler.getInstance().kickChannel(ctx.channel());
                break;
            //心跳应答
            case ChannelConst.CHANNEL_HEART:
                ProtocolManager.sendOkACK(ctx.channel(),ChannelConst.CHANNEL_HEART);
                break;
            //客户端发送消息
            case ChannelConst.CHANNEL_MSG:
                if(!ChannelConst.LOGIC_PROCESS.sendMsgBefore(ctx.channel(), protocol)){
                    return;
                }
                ProtocolManager.sendMsg(ctx.channel(),protocol);
                break;
            case ChannelConst.CHANNEL_ACK:
                ProtocolManager.ack(protocol);
                break;
            //业务层自定义的消息协议
            default:
                ChannelConst.LOGIC_PROCESS.customProtocolHandler(ctx.channel(),protocol);
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
        ChannelHandler.getInstance().kickChannel(ctx.channel());
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

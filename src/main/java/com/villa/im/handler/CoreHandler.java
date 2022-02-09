package com.villa.im.handler;

import com.villa.im.manager.ProtocolManager;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.LoginInfo;
import com.villa.im.model.Protocol;
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
        //如果当前请求不是登录 则都需要判断登录
        if(protocol.getType()!=ChannelConst.CHANNEL_LOGIN&&protocol.getType()!=ChannelConst.CHANNEL_HEART){
            //未登录
            if(!ChannelHandler.getInstance().isOnline(ctx.channel())){
                ProtocolManager.sendAck(ctx.channel(), ChannelConst.CHANNEL_NO_LOGIN);
                return;
            }
        }
        switch (protocol.getType()){
            //客户端登录
            case ChannelConst.CHANNEL_LOGIN:
                //客户端已经登录 不能重复登录
                if(ChannelHandler.getInstance().isOnline(ctx.channel())){
                    return;
                }
                //login前置
                if(!ChannelConst.LOGIC_PROCESS.loginBefore(ctx.channel(), protocol)){
                    return;
                }
                //获取登录信息
                LoginInfo loginInfo = ChannelConst.LOGIC_PROCESS.getLoginInfo(ctx.channel(), protocol);
                if(loginInfo==null||Util.isEmpty(loginInfo.getId())||Util.isEmpty(loginInfo.getDevice())){
                    //发送消息给客户端,需要连接标识符
                    ProtocolManager.sendAck(ctx.channel(), ChannelConst.CHANNEL_NOT_LOGIN_ID);
                    return;
                }
                //将连接信息存入连接属性中
                Util.putChannelInfo(ctx.channel(), loginInfo);
                //将连接保存
                ChannelHandler.getInstance().addChannel(ctx.channel());
                //发送请求结果给客户端
                ProtocolManager.sendAck(ctx.channel(),ChannelConst.CHANNEL_LOGIN_SUCCESS);
                break;
            //客户端退出登录
            case ChannelConst.CHANNEL_LOGOUT:
                //logout前置
                if(!ChannelConst.LOGIC_PROCESS.logoutBefore(ctx.channel(), protocol)){
                    return;
                }
                //踢掉客户端
                ChannelHandler.getInstance().kickChannel(ctx.channel());
                break;
            //心跳应答
            case ChannelConst.CHANNEL_HEART:
                ProtocolManager.sendAck(ctx.channel(),ChannelConst.CHANNEL_HEART);
                break;
            //客户端发送消息 单聊群聊都统一处理,只是在获取转发者时,业务层根据type来区分
            case ChannelConst.CHANNEL_ONE2ONE_MSG:
            case ChannelConst.CHANNEL_GROUP_MSG:
                //sendMag前置
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
        Log.log("【IM】新连接进入,当前连接数："+ ++channelCount);
    }

    /**
     * 连接断开的时候触发
     */
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String channelId = Util.getChannelId(ctx.channel());
        if(Util.isNotEmpty(channelId)){
            //触发事件
            ChannelConst.LOGIC_PROCESS.sessionClosed(Util.getChannelId(ctx.channel()),ctx.channel());
        }
        //断开连接 将登录者T掉
        ChannelHandler.getInstance().kickChannel(ctx.channel());
        Log.log("【IM】有连接断开,当前连接数:"+--channelCount);
    }

    /**
     * 出现异常时候触发,关闭当前连接
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String channelId = Util.getChannelId(ctx.channel());
        if(Util.isNotEmpty(channelId)){
            throw new RuntimeException(String.format("【IM】[%s]连接发生异常：%s",channelId,cause.getMessage()));
        }else{
            throw new RuntimeException(String.format("【IM】未登录的连接发生异常：%s",cause.getMessage()));
        }
    }
}

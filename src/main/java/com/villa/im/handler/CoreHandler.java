package com.villa.im.handler;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.process.LogicProcess;
import com.villa.im.protocol.Protocol;
import com.villa.im.protocol.ProtocolAction;
import com.villa.im.protocol.PrototolSate;
import com.villa.im.protocol.SimpleProtocol;
import com.villa.im.util.ChannelUtil;
import com.villa.im.util.Log;
import com.villa.im.util.Util;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * TCP/UDP/WS 统一的处理器
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class CoreHandler {
    private static int channelCount = 0;
    private LogicProcess logicProcess;
    protected void channelRead0(ChannelHandlerContext ctx, Protocol protocol) {
        Log.log("接收到客户端消息:"+JSON.toJSONString(protocol));
        switch (protocol.getType()){
            //客户端登录
            case ChannelConst.CHANNEL_LOGIN:
                //获取连接标识符
                String channelId = protocol.getFrom();
                if(!Util.isNotEmpty(channelId)){
                    //发送消息给客户端,需要连接标识符
                    ProtocolAction.send(ctx.channel(),new SimpleProtocol(ChannelConst.CHANNEL_LOGIN,ChannelConst.S2C_CHANNEL_ID_EMPTY),null);
                    return;
                }
                //将连接标识符存入连接属性中
                Util.putChannelId(ctx.channel(), channelId);
                //将连接保存
                ChannelUtil.getInstance().addChannel(ctx.channel());
                //发送请求结果给客户端
                ProtocolAction.sendOkACK(ctx.channel(), ChannelConst.CHANNEL_LOGIN, null);
                break;
            //客户端退出登录
            case ChannelConst.CHANNEL_LOGOUT:
                //踢掉客户端
                ChannelUtil.getInstance().kickChannel(ctx.channel());
                break;
            //心跳应答
            case ChannelConst.CHANNEL_HEART:
                ProtocolAction.sendOkACK(ctx.channel(),ChannelConst.CHANNEL_HEART,null);
                break;
            //客户端发送消息
            case ChannelConst.CHANNEL_MSG:
                //获取发送目标
                //通过业务处理器获取多个目标
                List<String> targets = logicProcess.getTargets(protocol.getTo());
                if (targets.size()==1){
                    sendMsg(targets,protocol, PrototolSate.ONE,targets.get(0));
                }
                targets.forEach(target->{
                    //放到线程中去 快一些（需要测试）
                    new Thread(()->{
                        ProtocolAction.sendMsg(targets,protocol,PrototolSate.N,target);
                    }).start();
                });
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
        ChannelUtil.getInstance().kickChannel(ctx.channel());
        Log.log("有连接断开,当前连接数:"+--channelCount);
    }

    /**
     * 出现异常时候触发,关闭当前连接
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.log(String.format("连接[%s]发生异常:%s",(String) ctx.channel().attr(ChannelConst.CHANNEL_ID).get(),cause.getMessage()));
    }

    /**
     * 设置逻辑业务处理器
     */
    public void setLogicProcess(LogicProcess logicProcess) {
        this.logicProcess = logicProcess;
    }
}

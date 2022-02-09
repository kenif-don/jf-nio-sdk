package com.villa.im.manager;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.Protocol;
import com.villa.im.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 专门处理发送的类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class SendManager {
    /**
     * 通用的发送数据方法
     * @param channel   客户端连接
     * @param protocol  发送的数据
     */
    public static void send(Channel channel, Object protocol){
        baseSend(channel,protocol).addListener((ChannelFutureListener) result -> {
            if(ChannelConst.LOGIC_PROCESS==null)return;
            //回调函数处理
            ChannelConst.LOGIC_PROCESS.sendCallBack(protocol);
        });
    }

    private static ChannelFuture baseSend(Channel channel, Object protocol) {
        switch (Util.getChannelProtoType(channel)){
            case WS:
                return channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(protocol)));
            case TCP:
            case UDP:
                return channel.writeAndFlush(protocol);
        }
        return null;
    }

    /**
     * 统一回复客户端的应答方法
     */
    public static void sendAck(Channel channel,int type){
        baseSend(channel,new Protocol(type));
    }
    public static void sendAck(Channel channel, Protocol protocol, int type){
        //这里为了不对原对象进行修改,所以新new一个对象赋值 其中type为ack类型 ack为1代表应答包
        baseSend(channel,new Protocol(type,protocol.getFrom(),protocol.getTo(),protocol.getData(),1,protocol.getNo()));
    }
}

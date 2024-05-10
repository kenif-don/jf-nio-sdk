package com.jf.im.manager;

import com.alibaba.fastjson2.JSON;
import com.jf.im.model.ChannelConst;
import com.jf.im.model.ProtoBuf;
import com.jf.im.model.Protocol;
import com.jf.im.util.NioUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 专门处理发送的类
 */
public class SendManager {
    /**
     * 通用的发送数据方法
     * @param channel   客户端连接
     * @param protocol  发送的数据
     */
    public static void send(Channel channel, Protocol protocol){
        baseSend(channel,protocol).addListener((ChannelFutureListener) result -> {
            if(ChannelConst.LOGIC_PROCESS==null)return;
            //回调函数处理
            ChannelConst.LOGIC_PROCESS.sendCallBack(protocol);
        });
    }

    private static ChannelFuture baseSend(Channel channel, Protocol protocol) {
        switch (NioUtil.getChannelProtoType(channel)){
            case WS:
                return channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(protocol)));
            case TCP:
            case UDP:
                switch (ChannelConst.DATA_PROTO_TYPE){
                    case JSON://如果使用json协议的编解码器
                        return channel.writeAndFlush(protocol);
                    case PROTOBUF://如果使用protobuf的编解码器
                        ProtoBuf.proto_my build = ProtoBuf.proto_my.newBuilder()
                                .setType(protocol.getType())
                                .setAck(protocol.getAck())
                                .setFrom(protocol.getFrom())
                                .setTo(protocol.getTo())
                                .setNo(protocol.getNo())
                                .setData(protocol.getData())
                                .setExt1(protocol.getExt1())
                                .setExt2(protocol.getExt2())
                                .setExt3(protocol.getExt3())
                                .setExt4(protocol.getExt4())
                                .setExt5(protocol.getExt5())
                                .build();
                        return channel.writeAndFlush(build);
                }
        }
        return null;
    }

    /**
     * 统一回复客户端的应答方法
     */
    public static void sendHeartbeat(Channel channel,int type){
        baseSend(channel,new Protocol(type));
    }

    /**
     * 给发送方的消息回执
     */
    public static void sendMsgAck(Channel channel, Protocol protocol){
        //这里为了不对原对象进行修改,所以新new一个对象赋值 其中type为ack类型 ack为1代表应答包
        Protocol pt = new Protocol(protocol.getType(), protocol.getFrom(), protocol.getTo(), protocol.getData(), 1, protocol.getNo());
        baseSend(channel,pt);
    }

    /***
     * 给请求方发送一条错误消息应答包
     * err模型
     * ack  -- 500
     * type -- 客户端请求的type
     * data -- 返回的错误消息
     */
    public static void sendErr(Channel channel,int type,String err_code){
        baseSend(channel,new Protocol(type,err_code,ChannelConst.ACK_ERR));
    }
    /** 给请求方发送一条成功消息应答包 */
    public static void sendSuccess(Channel channel,int type,String data){
        baseSend(channel,new Protocol(type,data,ChannelConst.ACK_SUCCESS));
    }
}

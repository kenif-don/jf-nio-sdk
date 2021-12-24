package com.villa.im.util;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.villa.im.handler.CoreHandler;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoBuf;
import com.villa.im.model.ProtoType;
import com.villa.im.model.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 统一工具类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Util {
    public static boolean isNotEmpty(String str){
        return str!=null&&!"".equals(str.trim());
    }
    public static  void channelRead(ChannelHandlerContext ctx, CoreHandler coreHandler, ByteBuf content) {
        switch (ChannelConst.DATA_PROTO_TYPE){
            case JSON:
                coreHandler.channelRead0(ctx, Util.byteBuf2ProtocolByJson(content));
                break;
            case PROTOBUF:
                coreHandler.channelRead0(ctx,Util.byteBuf2ProtocolByProtoBuf(content));
                break;
        }
    }
    public static Protocol byteBuf2ProtocolByProtoBuf(ByteBuf buf){
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        try {
            ProtoBuf.proto_my proto_my = ProtoBuf.proto_my.parseFrom(data);
            return new Protocol(proto_my.getType(),proto_my.getFrom(),proto_my.getTo(),proto_my.getData(),proto_my.getAck(),proto_my.getId());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 类型转换
     */
    public static Protocol byteBuf2ProtocolByJson(ByteBuf buf){
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return JSON.parseObject(new String(data, StandardCharsets.UTF_8),Protocol.class);
    }
    /**
     * 简单的封装 从客户端连接中获取客户端的标识符
     */
    public static String getChannelId(Channel channel){
        Object attr = channel.attr(ChannelConst.CHANNEL_ID).get();
        if(attr==null){
            return null;
        }
        return attr.toString();
    }

    /**
     * 往客户端连接中放标识符
     */
    public static void putChannelId(Channel channel,String channelId){
        channel.attr(ChannelConst.CHANNEL_ID).set(channelId);
    }
    /**
     * 简单的封装 从客户端连接中获取客户端协议类型
     */
    public static ProtoType getChannelProtoType(Channel channel){
        return (ProtoType) channel.attr(ChannelConst.PROTO_TYPE).get();
    }

    /**
     * 往客户端连接中放协议类型
     */
    public static void putChannelProtoType(Channel channel,ProtoType protoType){
        channel.attr(ChannelConst.PROTO_TYPE).set(protoType);
    }

    /**
     * 获取随机字符串 通过uuid生成
     */
    public static String getRandomStr(){
        return UUID.randomUUID().toString();
    }
    /** 客户端链接超时处理 */
    public static void userEventTriggered(ChannelHandlerContext ctx, Object evt,CoreHandler coreHandler){
        IdleStateEvent event = (IdleStateEvent)evt;
        if (event.state()== IdleState.READER_IDLE){
            String channelId = Util.getChannelId(ctx.channel());
            //触发事件
            if(Util.isNotEmpty(channelId)&&ChannelConst.LOGIC_PROCESS.channelTimeout(channelId,ctx.channel())){
                coreHandler.handlerRemoved(ctx);
            }
        }
    }
}

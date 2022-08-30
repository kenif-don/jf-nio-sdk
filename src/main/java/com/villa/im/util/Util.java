package com.villa.im.util;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.villa.im.handler.CoreHandler;
import com.villa.im.model.*;
import com.villa.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.nio.charset.StandardCharsets;

/**
 * 统一工具类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Util {
    public static boolean isEmpty(String str){
        return str==null||"".equals(str.trim());
    }
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
            return new Protocol(proto_my.getType(),proto_my.getFrom(),proto_my.getTo(),proto_my.getData(),proto_my.getAck(),proto_my.getNo());
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
     * 往客户端连接中放登录标志
     */
    public static void putChannelInfo(Channel channel, LoginInfo loginInfo){
        channel.attr(ChannelConst.CHANNEL_INFO).set(loginInfo);
    }
    /**
     * 往客户端连接中放协议类型
     */
    public static void putChannelProtoType(Channel channel,ProtoType protoType){
        channel.attr(ChannelConst.PROTO_TYPE).set(protoType);
    }
    /**
     * 简单的封装 从客户端连接中获取客户端的标识符
     */
    public static String getChannelId(Channel channel){
        LoginInfo attr = channel.attr(ChannelConst.CHANNEL_INFO).get();
        if(attr==null){
            return null;
        }
        return attr.getId();
    }
    /**
     * 简单的封装 从客户端连接中获取客户端协议类型
     */
    public static String getChannelDevice(Channel channel){
        LoginInfo attr = channel.attr(ChannelConst.CHANNEL_INFO).get();
        if(attr==null){
            return null;
        }
        return attr.getDevice();
    }
    /**
     * 简单的封装 从客户端连接中获取客户端协议类型
     */
    public static ProtoType getChannelProtoType(Channel channel){
        return channel.attr(ChannelConst.PROTO_TYPE).get();
    }
    /** 客户端链接超时处理 */
    public static void userEventTriggered(ChannelHandlerContext ctx, Object evt,CoreHandler coreHandler){
        IdleStateEvent event = (IdleStateEvent)evt;
        //没有登录过的才关闭链接  登录过的不处理
        if (event.state()== IdleState.READER_IDLE&&Util.isEmpty(Util.getChannelId(ctx.channel()))&&ctx.channel()!=null&&ctx.channel().isOpen()){
            Log.out("【IM】未登录的客户端超时退出");
            ctx.close();
        }
    }
}

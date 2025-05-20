package com.jf.im.util;

import com.alibaba.fastjson2.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jf.im.handler.ChannelHandler;
import com.jf.im.handler.CoreHandler;
import com.jf.im.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**统一工具类*/
public class NioUtil {
    public static void channelRead(ChannelHandlerContext ctx, CoreHandler coreHandler, ByteBuf content) {
        ProtoType protoType = NioUtil.getChannelProtoType(ctx.channel());
        DataProtoType dataProtoType = ChannelConst.DATA_PROTO_TYPE_MAP.get(protoType);
        switch (dataProtoType){
            case JSON:
                coreHandler.channelRead0(ctx, NioUtil.byteBuf2ProtocolByJson(content));
                break;
            case PROTOBUF:
                coreHandler.channelRead0(ctx, Objects.requireNonNull(NioUtil.byteBuf2ProtocolByProtoBuf(content)));
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
    public static void putChannelProtoType(Channel channel, ProtoType protoType){
        channel.attr(ChannelConst.PROTO_TYPE).set(protoType);
    }
    /**
     * 简单的封装 从客户端连接中获取客户端协议类型
     */
    public static ProtoType getChannelProtoType(Channel channel){
        return channel.attr(ChannelConst.PROTO_TYPE).get();
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
    public static List<Channel> getChannels(String channelId){
        return ChannelHandler.getInstance().getChannels(channelId);
    }
    public static Channel getChannels(String channelId,String device){
        return ChannelHandler.getInstance().getChannel(channelId,device);
    }
    /** 客户端链接超时处理 */
    public static void userEventTriggered(ChannelHandlerContext ctx){
        //登录过的关闭链接
        if (ctx.channel()!=null && !StringUtil.isNullOrEmpty(NioUtil.getChannelId(ctx.channel()))&& ctx.channel().isOpen()){
            System.out.println("超时："+ctx.channel());
            ctx.close();
        }
    }
}

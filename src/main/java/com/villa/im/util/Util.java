package com.villa.im.util;

import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.UnsupportedEncodingException;
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

    /**
     * 类型转换
     */
    public static String byteBuf2String(ByteBuf buf){
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        try {
            return new String(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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
}

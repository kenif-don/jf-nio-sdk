package com.villa.im.model;

import io.netty.util.AttributeKey;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ChannelConst {
    //协议类型key  值有tcp/udp/ws枚举
    public static AttributeKey PROTO_TYPE = AttributeKey.newInstance("proto_type");
    //每个客户端连接对应的唯一标识符  值自定义 可以是用户表的唯一主键
    public static AttributeKey CHANNEL_ID = AttributeKey.newInstance("channel_id");

    //-----------------消息协议--------------------------
    //客户端登录
    public static final int CHANNEL_LOGIN = 0;
    //客户端消息交互
    public static final int CHANNEL_MSG = 1;
    //接收到的客户端回执
    public static final int CHANNEL_ACK = 2;
    //接收到的客户端心跳
    public static final int CHANNEL_HEART = 3;
    //客户端正常退出
    public static final int CHANNEL_LOGOUT = 9;
}
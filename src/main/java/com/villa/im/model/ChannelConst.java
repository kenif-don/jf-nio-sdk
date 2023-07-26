package com.villa.im.model;

import com.villa.im.process.LogicProcess;
import io.netty.util.AttributeKey;

public class ChannelConst {
    /**
     * qos发送延迟 每3秒发送一次
     */
    public static int QOS_DELAY = 3*1000;
    //需要业务处定义的业务处理器 --默认实现
    public static LogicProcess LOGIC_PROCESS = new LogicProcess() {};
    //消息类型的协议类型 --默认json格式
    public static DataProtoType DATA_PROTO_TYPE = DataProtoType.JSON;
    //协议类型key  值有tcp/udp/ws枚举
    public static AttributeKey<ProtoType> PROTO_TYPE = AttributeKey.newInstance("proto_type");
    /**
     * 拥有两个属性
     * 1. 每个客户端连接对应的唯一标识符  值自定义 可以是用户表的唯一主键
     * 2. 设备号或设备类型值
     */
    public static AttributeKey<LoginInfo> CHANNEL_INFO = AttributeKey.newInstance("channel_info");

    //-----------------消息协议类型(500内保留给当前SDK做系统指令，自定义指令请使用101以上)--------------------------
    //客户端登录
    public static final int CHANNEL_LOGIN = 0;
    //单聊消息交互
    public static final int CHANNEL_ONE2ONE_MSG = 1;
    //消息回执
    public static final int CHANNEL_ACK = 2;
    //接收到的客户端心跳
    public static final int CHANNEL_HEART = 3;
    //群聊消息交互
    public static final int CHANNEL_GROUP_MSG = 8;
    //客户端正常退出
    public static final int CHANNEL_LOGOUT = 9;

    //------------------------------提示类协议类型------------------------------------------------------
    /** IM的统一错误码 */
    public static final int ACK_ERR = 500;
    /** 处理成功 */
    public static final int ACK_SUCCESS = 200;
}
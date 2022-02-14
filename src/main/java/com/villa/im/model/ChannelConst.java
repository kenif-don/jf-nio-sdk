package com.villa.im.model;

import com.villa.im.process.LogicProcess;
import io.netty.util.AttributeKey;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ChannelConst {
    /**
     * qos发送延迟
     */
    public static int QOS_DELAY = 2*1000;
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

    //-----------------消息协议类型(100内保留给当前SDK做系统指令，自定义指令请使用101以上)--------------------------
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
    /** 此操作需要客户端登录 但客户端未登录 */
    public static final int CHANNEL_NO_LOGIN = 11;
    /**
     * 登录失败的返回类型
     * 1. 登录时data不存在
     * 2. data中time字段无值
     * 3. data中param字段无值
     * 4. param中token无值
     * 5. param中id无值
     * 6. param中device无值
     * 7. param中token值无效
     */
    public static final int CHANNEL_NOT_LOGIN_ID = 12;
    /** 处理成功 */
    public static final int CHANNEL_LOGIN_SUCCESS = 13;
    /** 此消息必须携带消息ID,但未携带 */
    public static final int CHANNEL_MESSAGE_NO_ID = 14;
}
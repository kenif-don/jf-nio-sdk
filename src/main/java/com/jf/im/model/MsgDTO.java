package com.jf.im.model;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**暂存消息的包装类*/
@Data
@NoArgsConstructor
public class MsgDTO {
    private String device;
    //连接标志符 用来获取客户端通道
    private Channel channel;
    //消息体
    private Protocol protocol;
    /**
     * 上次这个包发送的时间戳
     * 此字段设计目的：
     * 定时器中消息每n s发送一次,而且是独立线程运行,可能会出现一种情况就是刚把这个消息包放入qos集合,而qos刚好ns到了
     * 就立马发送了,但是这个时候正常发送也是才发了,因为丢入qos时，正常也会发送一次
     * 应该让此消息至少延迟n s再进行第二次发送
     */
    private long preSendTimeStamp;
    private int count;//重试次数
    public MsgDTO(Channel channel, Protocol protocol,String device) {
        this.channel = channel;
        this.protocol = protocol;
        this.preSendTimeStamp = System.currentTimeMillis();
        this.device = device;
    }
}

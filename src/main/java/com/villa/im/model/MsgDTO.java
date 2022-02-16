package com.villa.im.model;

import io.netty.channel.Channel;

/**
 * 暂存消息的包装类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class MsgDTO {
    private String device;
    //连接标志符 用来获取客户端通道
    private Channel channel;
    //消息体
    private Protocol protocol;
    /**
     * 上次这个包发送的时间戳
     * 此字段设计目的：
     * 定时器中消息每2s发送一次,而且是独立线程运行,可能会出现一种情况就是刚把这个消息包放入qos集合,而qos刚好2s到了
     * 就立马发送了,但是这个时候正常发送也是才发了,因为丢入qos时，正常也会发送一次
     * 应该让此消息至少延迟2s再进行第二次发送
     */

    private long preSendTimeStamp;
    public MsgDTO(Channel channel, Protocol protocol,String device) {
        this.channel = channel;
        this.protocol = protocol;
        this.preSendTimeStamp = System.currentTimeMillis();
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getPreSendTimeStamp() {
        return preSendTimeStamp;
    }

    public void setPreSendTimeStamp(long preSendTimeStamp) {
        this.preSendTimeStamp = preSendTimeStamp;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}

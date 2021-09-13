package com.villa.im.model;

import com.villa.im.protocol.Protocol;

/**
 * 暂存消息的包装类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class MsgDTO {
    //连接标志符 用来获取客户端通道
    private String channelId;
    //消息体
    private Protocol protocol;
    public MsgDTO(String channelId, Protocol protocol) {
        this.channelId = channelId;
        this.protocol = protocol;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}

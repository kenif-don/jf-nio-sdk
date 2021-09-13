package com.villa.im.model;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端连接的包装类
 * 此类中包含了最少一个最多3个协议的客户端连接 tcp/udp/ws
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ChannelDTO {
    private Map<ProtoType, Channel> channels = new HashMap<>();
    //按照协定的udp>tcp>ws规则获取客户端连接对象
    public Channel getChannelUDPFirst(){
        if(channels.containsKey(ProtoType.UDP)){
            return channels.get(ProtoType.UDP);
        }else if(channels.containsKey(ProtoType.TCP)){
            return channels.get(ProtoType.TCP);
        }else{
            return channels.get(ProtoType.WS);
        }
    }

    public void putChannel(ProtoType protoType,Channel channel){
        channels.put(protoType,channel);
    }
    public Channel getChannel(ProtoType protoType){
        return channels.get(protoType);
    }

    public Channel removeChannel(ProtoType protoType){
        return channels.remove(protoType);
    }
    public Map<ProtoType, Channel> getChannels() {
        return channels;
    }
}

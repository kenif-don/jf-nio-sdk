package com.jf.im.model;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端连接的包装类
 * 此类中包含了最少一个客户端连接 key为客户端标志 可以是设备号 也可以是app/pc/web等类型值
 * 若为设备号,那同类型设备可同时存在多个  比如手机端可以同时存在多个,pc/web端同理
 * 若为类型值,那么同类型设备仅能存在一个  就类似微信了
 */
public class ChannelDTO {
    //HashMap有线程安全问题
    private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    public List<Channel> getChannels(){
        return new ArrayList<>(channels.values());
    }

    public void putChannel(String device,Channel channel){
        channels.put(device,channel);
    }
    public Channel getChannel(String device){
        return channels.get(device);
    }
    public Channel removeChannel(String device){
        return channels.remove(device);
    }

    public List<Channel> getChannelsButMe(String device) {
        List<Channel> butMeChannels = new ArrayList<>();
        channels.keySet().forEach(key->{
            if(!key.equals(device)){
                butMeChannels.add(channels.get(key));
            }
        });
        return butMeChannels;
    }
}

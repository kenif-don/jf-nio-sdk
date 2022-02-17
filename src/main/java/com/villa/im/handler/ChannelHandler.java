package com.villa.im.handler;

import com.villa.im.model.ChannelDTO;
import com.villa.im.util.IMLog;
import com.villa.im.util.Util;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 饿汉单例  这里不适应静态类的原因是个人认为静态类中放的都应该是一些独立的方法 也就是不需要跟其他方法或属性有太多关联的东西
 * 比如工具类  一般每个方法会成为一个独立的功能，不会与太多其类属性或依赖的对象产生太多交集
 * 而当前这个类是用来管理客户端连接的，几乎所有方法都会对客户端连接对象进行关联，所以使用单例
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ChannelHandler {
    private static ChannelHandler channelHandler = new ChannelHandler();
    /**
     * 所有在线的客户端连接
     * key为用户唯一主键
     */
    private ConcurrentMap<String, ChannelDTO> channels = new ConcurrentHashMap<>();

    /**
     * 添加新的连接
     *
     * 当前框架支持客户端同时拥有多个 而且消息之间是互通的 框架会根据设备号或设备类型值自动进行区分
     * 需要先判断此链接是否已经添加 也就是单点判断(同设备号/同设备类型值仅能存在一个链接)
     * 如果存在在线的  需要判断两个链接是否一致 否则将老的链接T掉
     */
    public void addChannel(Channel channel){
        String channelId = Util.getChannelId(channel);
        //先验证同设备号的是否在线
        if(isOnline(channel)){
            //上一次在线的链接
            Channel oldChannel = getChannelById(channel);
            //如果两次链接相等  代表是客户端发送了登录的重复请求 不做处理就行
            if(oldChannel.compareTo(channel)==0){
                IMLog.log("两次登录都是同一个连接发起,客户端重复发送了登录请求,不做任何处理");
                return;
            }
            //这里代表客户端连接是一个新的连接或者新的设备 需要将老的链接T掉
            kickChannel(channel);
        }
        ChannelDTO channelDTO = channels.get(channelId);
        if(channelDTO==null){
            channelDTO = new ChannelDTO();
        }
        //不在线或被T掉或不同协议登录后的处理 直接存起来
        channelDTO.putChannel(Util.getChannelDevice(channel),channel);
        channels.put(channelId,channelDTO);
        IMLog.log("【IM】【%s】上线",channelId);
        printOlineCount();
    }
    public void printOlineCount(){
        IMLog.log("【IM】当前【"+channels.size()+"】人在线");
    }
    /**
     * 踢掉客户端连接
     */
    public void kickChannel(Channel channel){
        String channelId = Util.getChannelId(channel);
        //登录过的才从集合中删除 未登录的 直接关闭链接即可
        if(Util.isNotEmpty(channelId)&&channels.get(channelId)!=null){
            //下面两句代码有线程安全问题 将HashMap换成ConcurrentHashMap
            channels.get(channelId).removeChannel(Util.getChannelDevice(channel));
            //也就是这个标识符对应的连接全部没有了
            if(channels.get(channelId).getChannels().size()==0){
                //就把channels中的也进行删除
                channels.remove(channelId);
            }
            IMLog.log(String.format("【IM】客户端[%s]下线", channelId));
        }
        printOlineCount();
        channel.close();
    }
    /**
     * 获取指定用户的所有链接
     */
    public List<Channel> getChannels(String channelId){
        return channels.get(channelId).getChannels();
    }

    /**
     * 获取指定用户的所有链接 不包括自己
     */
    public List<Channel> getChannels(String channelId,String device){
        return channels.get(channelId).getChannelsButMe(device);
    }
    /**
     * 根据客户端标志符和客户端连接对应的设备号/设备类型值获取客户端连接
     */
    public Channel getChannelById(Channel channel){
        return channels.get(Util.getChannelId(channel)).getChannel(Util.getChannelDevice(channel));
    }

    /**
     * 判断当前链接是否已存在 在线
     * 根据标识符和协议类型寻找
     */
    public boolean isOnline(Channel channel){
        String channelId = Util.getChannelId(channel);
        if(Util.isEmpty(channelId)){
            return false;
        }
        ChannelDTO channelDTO = channels.get(channelId);
        if(channelDTO==null) {
            return false;
        }
        Channel onLineChannel = channelDTO.getChannel(Util.getChannelDevice(channel));
        if(onLineChannel==null) {
            return false;
        }
        return onLineChannel.isActive();
    }
    /**
     * 根据标识符寻找 只要只是在线一种协议就算在线
     */
    public boolean isOnline(String channelId){
        ChannelDTO channelDTO = channels.get(channelId);
        return Util.isNotEmpty(channelId)&&channelDTO!=null&&channelDTO.getChannels().size()>0;
    }
    private ChannelHandler(){

    }
    public static ChannelHandler getInstance(){
        return channelHandler;
    }
}

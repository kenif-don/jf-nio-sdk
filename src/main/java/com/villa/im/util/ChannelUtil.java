package com.villa.im.util;

import com.villa.im.model.ChannelConst;
import com.villa.im.model.ChannelDTO;
import com.villa.im.protocol.CallBackAction;
import com.villa.im.protocol.ProtocolAction;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 饿汉单例  这里不适应静态类的原因是个人认为静态类中放的都应该是一些独立的方法 也就是不需要跟其他方法或属性有太多关联的东西
 * 比如工具类  一般每个方法会成为一个独立的功能，不会与太多其类属性或依赖的对象产生太多交集
 * 而当前这个类是用来管理客户端连接的，几乎所有方法都会对客户端连接对象进行关联，所以使用单例
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ChannelUtil {
    private static ChannelUtil channelUtil = new ChannelUtil();
    /**
     * 所有在线的客户端连接
     */
    private ConcurrentMap<String, ChannelDTO> channels = new ConcurrentHashMap<>();

    /**
     * 添加新的连接
     *
     * 当前框架支持客户端同时拥有多种协议  比如客户端使用tcp做一对一聊天 使用udp做群聊 ws用于web聊天 而且消息之间是互通的
     * 所以可能会导致一个客户的最多拥有3个Channel 而当前框架会自动为连接标识符添加协议后缀进行区分
     * 而且当服务器收到消息转发的请求时：如果转发对象大于1,默认会使用udp协议,但如果目标客户端未开启udp协议
     * 那么按照udp>tcp>ws的优先级进行轮询得到连接对象后再发送
     *
     * 需要先判断此链接是否已经添加 也就是单点(同协议情况下,运行多种不同协议同时在线)判断
     * 如果存在在线的  需要判断两个链接是否一致 否则将老的链接T掉
     */
    public void addChannel(Channel channel){
        String channelId = Util.getChannelId(channel);
        //先验证同协议的是否在线
        if(isOnline(channelId)){
            //同协议的两次登录请求
            if(isOnline(channel)){
                //上一次在线的链接
                Channel oldChannel = getChannelById(channel);
                //如果两次链接相等  代表是客户端发送了登录的重复请求 不做处理就行
                if(oldChannel.compareTo(channel)==0){
                    Log.log("两次登录都是同一个连接发起,客户端重复发送了登录请求,不做任何处理");
                    return;
                }
                //这里代表客户端连接是一个新的连接或者新的设备 需要将老的/同协议的链接T掉
                kickChannel(channel);
            }else{
                //不同协议的两次登录请求  这里其实不需要操作 只是记录日志
                Log.log(String.format("[%s]登录%s连接",channelId,Util.getChannelProtoType(channel).name()));
            }
        }
        ChannelDTO channelDTO = channels.get(channelId);
        if(channelDTO==null){
            channelDTO = new ChannelDTO();
        }
        //不在线或被T掉或不同协议登录后的处理 直接存起来
        channelDTO.putChannel(Util.getChannelProtoType(channel),channel);
        channels.put(channelId,channelDTO);
        printOlineCount();
    }
    public void printOlineCount(){
        Log.log("当前客户端连接数:"+channels.size());
    }
    /**
     * 踢掉客户端连接
     */
    public void kickChannel(Channel channel){
        String channelId = Util.getChannelId(channel);
        if (!Util.isNotEmpty(channelId)){
            Log.log("当前连接标识符不存在,不进行踢人操作");
            return;
        }
        //从集合中删除
        channels.get(channelId).removeChannel(Util.getChannelProtoType(channel));
        //也就是这个标识符对应的连接全部没有了
        if(channels.get(channelId).getChannels().size()==0){
            //就把channels中的也进行删除
            channels.remove(channelId);
        }
        if(channel.isOpen()){
            //关闭客户端链接 关闭之前先通知客户端 已退出登录
            ProtocolAction.sendOkACK(channel, ChannelConst.CHANNEL_LOGOUT, new CallBackAction() {
                @Override
                public void callBack(boolean success, Channel channel, Object protocol) {
                    if(success){
                        channel.close();
                    }
                }
            });
        }
        Log.log(String.format("客户端[%s]被踢下线", channelId));
        printOlineCount();
    }
    /**
     * 获取单聊优先级最高的协议
     */
    public Channel getChannelTCPFirst(String channelId){
        return channels.get(channelId).getChannelTCPFirst();
    }
    /**
     * 获取群聊优先级最高的协议
     */
    public Channel getChannelUDPFirst(String channelId){
        return channels.get(channelId).getChannelUDPFirst();
    }
    /**
     * 根据客户端标志符和客户端连接对应的协议获取客户端连接
     */
    public Channel getChannelById(Channel channel){
        return channels.get(Util.getChannelId(channel)).getChannel(Util.getChannelProtoType(channel));
    }

    /**
     * 判断当前链接是否已存在 在线
     * 根据标识符和协议类型寻找
     */
    public boolean isOnline(Channel channel){
        String channelId = Util.getChannelId(channel);
        if(!Util.isNotEmpty(channelId)){
            return false;
        }
        ChannelDTO channelDTO = channels.get(channelId);
        return Util.isNotEmpty(channelId)&&channelDTO!=null&&channelDTO.getChannel(Util.getChannelProtoType(channel))!=null;
    }
    /**
     * 根据标识符寻找 只要只是在线一种协议就算在线
     */
    public boolean isOnline(String channelId){
        ChannelDTO channelDTO = channels.get(channelId);
        return Util.isNotEmpty(channelId)&&channelDTO!=null&&channelDTO.getChannels().size()>0;
    }
    private ChannelUtil(){

    }
    public static ChannelUtil getInstance(){
        return channelUtil;
    }
}

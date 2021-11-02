package com.villa.im.manager;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.MsgDTO;
import com.villa.im.model.Protocol;
import com.villa.im.handler.ChannelHandler;
import com.villa.im.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类包含了服务器往客户端发送的所有功能
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ProtocolManager {
    static{
        //原生定时器
        new Timer().schedule(new TimerTask() {
            public void run() {
                //获取待发消息集合的迭代器
                Iterator<MsgDTO> iterator = msgs.values().iterator();
                while (iterator.hasNext()){
                    MsgDTO msgDTO = iterator.next();
                    //如果在线就发送待发消息
                    if(ChannelHandler.getInstance().isOnline(msgDTO.getChannelId())){
                        Channel realChannel = ChannelHandler.getInstance().getChannelUDPFirst(msgDTO.getChannelId());;
                        baseSend(realChannel, msgDTO.getProtocol());
                    }else{
                        //如果不在线  就将当前待发消息直接删除
                        msgs.remove(msgDTO.getProtocol().getMsgNo());
                        //qos是在线才会触发 这里不在线代表 之前在线 后来不在线了 算是失败了 提供回调到业务层
                        ChannelConst.LOGIC_PROCESS.sendFailCallBack(msgDTO.getProtocol());
                    }
                }
            }
        },500,2*1000);
    }
    //待转发的消息集合
    private static ConcurrentHashMap<String, MsgDTO> msgs = new ConcurrentHashMap<>();

    /**
     * 接收到客户端回执
     */
    public static void ack(Protocol protocol) {
        //只需要删除对应的待发消息就行了--消息补偿流程就结束了
        if(Util.isNotEmpty(protocol.getMsgNo())){
            MsgDTO msgDTO = msgs.remove(protocol.getMsgNo());
            //成功回调
            ChannelConst.LOGIC_PROCESS.sendSuccessCallBack(msgDTO.getProtocol());
        }
    }
    /**
     * 统一的消息转发
     */
    public static void sendMsg(Channel channel, Protocol protocol){
        //聊天消息必须携带一个消息ID 如果没有就回执报错
        if(!Util.isNotEmpty(protocol.getMsgNo())){
            ProtocolManager.sendAck(channel,ChannelConst.MESSAGE_NO_ID, ChannelConst.CHANNEL_MSG);
            return;
        }
        //先给发送方一个消息回执，代表服务器收到了消息
        ProtocolManager.sendAck(channel,protocol,ChannelConst.CHANNEL_MSG);
        //判断是否已经存在待发送消息
        //判断这条消息是否已存在，如果已存在不做任何处理，否则会出现消息重复
        if(msgs.contains(protocol.getMsgNo())){
            return;
        }
        //因为上面给发送者回执时 ack被设置为1  所以这里需要改回来
        protocol.setAck(0);
        /**
         * 不管是否在线 而且不管是否转发成功  都需要  --存到消息记录中
         * 这里不在回调函数中执行 是因为在回调中可能被执行多次 这样会导致添加消息的方法会被调用多次
         * 而回调函数一般用来做离线消息存储 或push推送等
         */
        ChannelConst.LOGIC_PROCESS.addMessage(protocol);
        //当消息不存在的时候 将当前消息作为当前客户端的待发消息进行存储，等待回执删除
        /**
         * 这里需要启动消息补偿机制，只需要将消息存入待转发消息集合就行
         * 但是这里的设计是：
         * 1。 对于发送着来说，会将发送者携带的消息编号存入集合 作为带转发消息（其实已经发了，只是需要等到客户端回执才会清除这个消息）而服务器会
         *     给客户端进行回执，告诉客户端服务器已经收到了你的消息，但是客户端还需要给服务器一个回执，让服务器清除这个待发送消息 形成完整闭环
         *     而客户端回执的这个消息又会与 需转发客户端的回执一样，服务器也就采用同样的处理，进行消息删除
         * 2。 对于需转发客户端来说，消息编号会新生成一个，与接收到的不同，而且不同的客户端生成不同的消息编号，与客户端对应
         */
        msgs.put(protocol.getMsgNo(),new MsgDTO(Util.getChannelId(channel),protocol));

        //------------------------处理转发逻辑-----------------
        //通过业务处理器获取多个目标并转发和qos
        ChannelConst.LOGIC_PROCESS.getTargets(protocol).forEach(target->{
            sendMsg(target,protocol);
        });
    }

    /**
     * 提供内外皆可调用的通用方法
     * 此方法会开启qos和结果回调
     */
    public static void sendMsg(String channelId, Protocol protocol){
        //利用线程池加速
        ThreadManager.getInstance().execute(()->{
            //判断目标是否在线 而且不是自己 自己不能给自己发消息
            if(ChannelHandler.getInstance().isOnline(channelId)&&!channelId.equals(protocol.getFrom())){
                //将待转发消息存起来 给每个客户端对应当前消息生成一个唯一消息编号
                String new_msg_no = Util.getRandomStr();
                //将新的消息编号设置到消息中，替换原来的消息编号  原来的消息编号只与发送它的客户端对应
                protocol.setMsgNo(new_msg_no);
                msgs.put(new_msg_no,new MsgDTO(channelId,protocol));
                //获取优先级最高的协议
                Channel realChannel = ChannelHandler.getInstance().getChannelUDPFirst(channelId);
                //直接发送 这里如果发送失败，会有补偿机制去做重发  成功不需要做什么操作
                send(realChannel, protocol);
                return;
            }
            //对方客户端不在线 则直接调用回调函数通知
            ChannelConst.LOGIC_PROCESS.sendFailCallBack(protocol);
        });
    }
    /**
     * 通用的发送数据方法
     * @param channel   客户端连接
     * @param protocol  发送的数据
     */
    public static void send(Channel channel,Object protocol){
        baseSend(channel,protocol).addListener((ChannelFutureListener) result -> {
            if(ChannelConst.LOGIC_PROCESS==null)return;
            //回调函数处理
            ChannelConst.LOGIC_PROCESS.sendCallBack(protocol);
        });
    }

    private static ChannelFuture baseSend(Channel channel,Object protocol) {
        switch (Util.getChannelProtoType(channel)){
            case WS:
                return channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(protocol)));
            case TCP:
            case UDP:
                return channel.writeAndFlush(protocol);
        }
        return null;
    }

    /**
     * 统一回复客户端的应答方法
     */
    public static void sendOkACK(Channel channel,int type){
        baseSend(channel,new Protocol(type));
    }
    /**
     * 统一的带消息的ack应答
     */
    public static void sendAck(Channel channel, String dataContent, int type){
        Protocol protocol = new Protocol();
        protocol.setType(type);
        protocol.setDataContent(dataContent);
        protocol.setAck(1);
        baseSend(channel,protocol);
    }
    public static void sendAck(Channel channel, Protocol protocol, int type){
        protocol.setAck(1);
        protocol.setType(type);
        baseSend(channel,protocol);
    }
    public static void sendAck(Channel channel, Protocol protocol){
        protocol.setAck(1);
        baseSend(channel,protocol);
    }
}
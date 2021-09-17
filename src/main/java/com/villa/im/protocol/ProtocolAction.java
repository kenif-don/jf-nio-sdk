package com.villa.im.protocol;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.MsgDTO;
import com.villa.im.model.Protocol;
import com.villa.im.process.LogicProcess;
import com.villa.im.util.ChannelUtil;
import com.villa.im.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类包含了服务器往客户端发送的所有功能
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ProtocolAction {
    static{
        //原生定时器
        new Timer().schedule(new TimerTask() {
            public void run() {
                //获取待发消息集合的迭代器
                Iterator<MsgDTO> iterator = msgs.values().iterator();
                while (iterator.hasNext()){
                    MsgDTO msgDTO = iterator.next();
                    //如果在线就发送待发消息
                    if(ChannelUtil.getInstance().isOnline(msgDTO.getChannelId())){
                        Channel realChannel = ChannelUtil.getInstance().getChannelUDPFirst(msgDTO.getChannelId());;
                        send(realChannel, msgDTO.getProtocol(),null);
                    }else{
                        //如果不在线  就将当前待发消息直接删除 等到下一次客户端上线时，自己去拉消息记录即可
                        msgs.remove(msgDTO.getProtocol().getMsgNo());
                    }
                }
            }
        },2*1000);
    }
    //待转发的消息集合
    private static ConcurrentHashMap<String, MsgDTO> msgs = new ConcurrentHashMap<>();

    /**
     * 接收到客户端回执
     */
    public static void ack(Protocol protocol) {
        //只需要删除对应的待发消息就行了--消息补偿流程就结束了
        msgs.remove(protocol.getMsgNo());
    }
    /**
     * 统一的消息转发
     */
    public static void sendMsg(Channel channel, Protocol protocol, LogicProcess logicProcess){
        //先给发送方一个消息回执，代表服务器收到了消息
        ProtocolAction.sendOkACK(channel,ChannelConst.CHANNEL_MSG);
        //判断是否已经存在待发送消息
        //判断这条消息是否已存在，如果已存在不做任何处理，否则会出现消息重复
        if(msgs.contains(protocol.getMsgNo())){
            return;
        }
        //不管是否在线 而且不管是否转发成功  都需要  --存到消息记录中
        logicProcess.addMessage(protocol);
        //当消息不存在的时候 将当前消息作为当前客户端的待发消息进行存粗，等待回执删除
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
        //获取发送目标
        //通过业务处理器获取多个目标
        List<String> targets = logicProcess.getTargets(protocol.getTo());
        if (targets.size()==1){
            ProtocolAction.sendMsg(targets.get(0),protocol);
        }
        targets.forEach(target->{
            //放到线程中去 快一些（需要测试）
            new Thread(()->{
                ProtocolAction.sendMsg(target,protocol);
            }).start();
        });
    }
    private static void sendMsg(String channelId,Protocol protocol){
        //判断目标是否在线
        if(ChannelUtil.getInstance().isOnline(channelId)){
            //将待转发消息存起来 给每个客户端对应当前消息生成一个唯一消息编号
            String new_msg_no = Util.getRandomStr();
            //将新的消息编号设置到消息中，替换原来的消息编号  原来的消息编号只与发送它的客户端对应
            protocol.setMsgNo(new_msg_no);
            msgs.put(new_msg_no,new MsgDTO(channelId,protocol));
            //获取优先级最高的协议
            Channel realChannel = ChannelUtil.getInstance().getChannelUDPFirst(channelId);
            //直接发送 这里如果发送失败，会有补偿机制去做重发  成功不需要做什么操作
            send(realChannel, protocol,null);
        }
    }
    /**
     * 通用的发送数据方法
     * @param channel   客户端连接
     * @param protocol  发送的数据
     * @param callBack  回调函数
     */
    public static void send(Channel channel,Object protocol,CallBackAction callBack){
        ChannelFuture cf = baseSend(channel,protocol);
        cf.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture result) throws Exception {
                if(callBack==null)return;
                //直接交给回调函数处理
                callBack.callBack(result.isSuccess(),channel,protocol);
            }
        });
    }

    private static ChannelFuture baseSend(Channel channel,Object protocol) {
        switch (Util.getChannelProtoType(channel)){
            case WS:
                return channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(protocol)));
            case TCP:
                return channel.writeAndFlush(protocol);
            case UDP:
                break;
        }
        return null;
    }

    /**
     * 统一回复客户端的应答方法
     */
    public static void sendOkACK(Channel channel,int type){
        baseSend(channel,new Protocol(type));
    }
}

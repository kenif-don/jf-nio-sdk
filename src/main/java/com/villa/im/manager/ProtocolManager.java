package com.villa.im.manager;

import com.villa.im.handler.ChannelHandler;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.MsgDTO;
import com.villa.im.model.Protocol;
import com.villa.im.util.Util;
import io.netty.channel.Channel;

import java.util.List;

/**
 * 这个类包含了服务器往客户端发送的所有功能
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ProtocolManager {
    /**
     * 接收到客户端回执
     */
    public static void ack(Channel channel,Protocol protocol) {
        //只需要删除对应的待发消息就行了--消息补偿流程就结束了
        if(Util.isNotEmpty(protocol.getNo())){
            MsgDTO msgDTO = QosManager.removeQosMessage(channel,protocol);
            //成功回调
            ChannelConst.LOGIC_PROCESS.sendSuccessCallBack(msgDTO.getProtocol());
        }
    }
    /**
     * 统一的消息处理
     */
    public static void handlerMsg(Channel channel, Protocol protocol){
        //开启了qos 客户端需要一个回执
        if(protocol.getAck()==100){
            //聊天消息必须携带一个消息ID 如果没有就回执报错
            if(!Util.isNotEmpty(protocol.getNo())){
                SendManager.sendAck(channel,ChannelConst.CHANNEL_MESSAGE_NO_ID);
                return;
            }
            //先给发送方一个消息回执，代表服务器收到了消息
            SendManager.sendAck(channel,protocol,ChannelConst.CHANNEL_ACK);
            //判断是否已经存在待发送消息
            //判断这条消息是否已存在，如果已存在不做任何处理，否则会出现消息重复
            if(QosManager.getMsgs().containsKey(protocol.getNo())){
                return;
            }
            /**
             * 当消息不存在的时候 将当前消息作为当前客户端的待发消息进行存储，等待回执删除 这是回执给发送者的qos消息包
             * 这里需要启动消息补偿机制，只需要将消息存入待转发消息集合就行
             * 但是这里的设计是：
             * 1。 对于发送着来说，会将发送者携带的消息编号存入集合 作为带转发消息（其实已经发了，只是需要等到客户端回执才会清除这个消息）而服务器会
             *     给客户端进行回执，告诉客户端服务器已经收到了你的消息，但是客户端还需要给服务器一个回执，让服务器清除这个待发送消息 形成完整闭环
             *     而客户端回执的这个消息又会与 需转发客户端的回执一样，服务器也就采用同样的处理，进行消息删除
             * 2。 对于需转发客户端来说，消息编号会新生成一个，与接收到的不同，而且不同的客户端生成不同的消息编号，与客户端对应
             */
            QosManager.putQosQueue(channel,protocol);
        }
        /**
         * 不管是否在线 而且不管是否转发成功  都需要  --存到消息记录中
         * 这里不在回调函数中执行 是因为在回调中可能被执行多次 这样会导致添加消息的方法会被调用多次
         * 而回调函数一般用来做离线消息存储 或push推送等
         */
        ChannelConst.LOGIC_PROCESS.addMessage(protocol);
        //------------------------处理转发逻辑-----------------
        //利用线程池加速
        ThreadManager.getInstance().execute(()-> {
            //通过业务处理器获取多个目标并转发和qos
            ChannelConst.LOGIC_PROCESS.getTargets(protocol).forEach(target -> {
                sendMsg(target, protocol);
            });
            /**
             * 这里同时需要转发给"自己"的其他设备,其实与其他人是一样的操作
             */
            List<Channel> channels = ChannelHandler.getInstance().getChannels(Util.getChannelId(channel), Util.getChannelDevice(channel));
            channels.forEach(myChannel->{
                if(protocol.getAck()==100) {
                    //需要qos 才加入到qos队列,否则就不加入
                    QosManager.putQosQueue(channel,protocol);
                }
                SendManager.send(myChannel, protocol);
            });
        });
    }

    /**
     * 提供内外皆可调用的通用方法
     * 此方法会开启qos和结果回调
     */
    public static void sendMsg(String channelId, Protocol protocol){
        //判断目标是否在线 而且不是自己 自己不能给自己发消息
        if(ChannelHandler.getInstance().isOnline(channelId)&&!channelId.equals(protocol.getFrom())){
            //客户端要求启用qos机制才启用 ack==100
            //获取转发目标 1-N个
            List<Channel> channels = ChannelHandler.getInstance().getChannels(channelId);
            channels.forEach(realChannel->{
                if(protocol.getAck()==100) {
                    //需要qos 才加入到qos队列,否则就不加入
                    QosManager.putQosQueue(realChannel,protocol);
                }
                SendManager.send(realChannel, protocol);
            });
            return;
        }
        //对方客户端不在线 则直接调用失败回调函数通知
        ChannelConst.LOGIC_PROCESS.sendFailCallBack(channelId,protocol);
    }
}

package com.villa.im.manager;

import com.villa.im.handler.ChannelHandler;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.MsgDTO;
import com.villa.im.model.Protocol;
import com.villa.im.util.IMLog;
import com.villa.im.util.Util;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将QOS策略单独抽取出来处理
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class QosManager {
    static{
        /**
         * 原生定时器实现qos每间隔指定时间进行第N次发送,一直到qos中删除这个包或用户端掉线
         * 这里与主线程并行，所以可能会出现：主线程发送了消息后,qos立马又发送了一条，
         * 且间隔未达到ChannelConst.QOS_DELAY指定间隔,所以通过preSendTimeStamp字段来排除这种情况
         * 以保证qos的消息，至少间隔ChannelConst.QOS_DELAY指定时间发送
         *
         * 当前设计中并没有限制qos发送次数,也就是如果用户端未返回ack,那么这个消息包会一直间隔发送
         * 这里为什么不对发送次数进行限制的原因是：用户端弱网，但并没有掉线/断线 如果达到次数限制，记录为离线消息。
         * 但是用户端并掉线/断线，从而没有触发重连机制，就导致这个这次消息不会实时更新到用户端，虽然消息被记录为离线消息。
         * 但是用户端不会触发去拉去离线消息机制，如果等到用户下一次上线或下一次拉取离线时，这条消息已经被后面的新消息顶到看不见了....
         */
        new Timer().schedule(new TimerTask() {
            public void run() {
                //获取待发消息集合的迭代器
                Iterator<MsgDTO> iterator = msgs.values().iterator();
                while (iterator.hasNext()){
                    MsgDTO msgDTO = iterator.next();
                    //如果在线就发送待发消息
                    if(ChannelHandler.getInstance().isOnline(msgDTO.getChannel())){
                        //当前发送时间必须比上次发送时间至少间隔ChannelConst.QOS_DELAY
                        long cur_timestamp = System.currentTimeMillis();
                        if(System.currentTimeMillis()-msgDTO.getPreSendTimeStamp()< ChannelConst.QOS_DELAY){
                            continue;
                        }
                        Channel realChannel= msgDTO.getChannel();
                        //记录当前发送时间
                        msgDTO.setPreSendTimeStamp(cur_timestamp);
                        SendManager.send(realChannel, msgDTO.getProtocol());
                        IMLog.log("【IM】QOS推送一条消息,消息ID为【%s】",msgDTO.getProtocol().getNo());
                    }else{
                        //如果不在线  就将当前待发消息直接删除
                        msgs.remove(msgDTO.getProtocol().getNo());
                        //qos是在线才会触发 这里不在线代表 之前在线 后来不在线了 算是失败了 提供回调到业务层
                        ChannelConst.LOGIC_PROCESS.sendFailCallBack(Util.getChannelId(msgDTO.getChannel()),msgDTO.getProtocol());
                        IMLog.log("【IM】此【%s】目标不存在，消息【%s】可做离线处理",msgDTO.getProtocol().getTo(),msgDTO.getProtocol().getNo());
                    }
                }
            }
        },500,ChannelConst.QOS_DELAY);
    }
    //待转发的消息集合
    private static ConcurrentHashMap<String, MsgDTO> msgs = new ConcurrentHashMap<>();
    /** 将消息添加qos队列--这里使用map来装 */
    public static void putQosQueue(Channel channel, Protocol protocol){
        msgs.put(protocol.getNo()+"_"+Util.getChannelDevice(channel), new MsgDTO(channel,protocol));
        IMLog.log("【IM】将消息【%s】加入到QOS队列中,当前队列中消息数为:【%d】",protocol.getNo(),msgs.size());
    }
    public static ConcurrentHashMap<String, MsgDTO> getMsgs(){
        return msgs;
    }
    /** 从qos中删除对应的消息 */
    public static MsgDTO removeQosMessage(Channel channel,Protocol protocol){
        return msgs.remove(protocol.getNo()+"_"+Util.getChannelDevice(channel));
    }
    /** 判断是否包含 */
    public static boolean contains(Channel channel,Protocol protocol){
        return msgs.containsKey(protocol.getNo()+"_"+Util.getChannelDevice(channel));
    }
}

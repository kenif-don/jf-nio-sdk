package com.villa.im.protocol;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.process.LogicProcess;
import com.villa.im.util.ChannelUtil;
import com.villa.im.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * 这个类包含了服务器往客户端发送的所有功能
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ProtocolAction {
    /**
     * 统一的消息转发
     */
    private static void sendMsg(List<String> targets,Protocol protocol,PrototolSate prototolSate,String channelId){
        //判断目标是否在线
        if(ChannelUtil.getInstance().isOnline(targets.get(0))){
            //获取单聊优先级最高的协议
            Channel realChannel = null;
            switch (prototolSate){
                case ONE:
                    realChannel = ChannelUtil.getInstance().getChannelTCPFirst(channelId);
                    break;
                case N:
                    realChannel = ChannelUtil.getInstance().getChannelUDPFirst(channelId);
                    break;
            }
            send(realChannel, protocol, new CallBackAction() {
                public void callBack(boolean success, Channel channel, Object protocol) {
                    if(success){
                        //发送成功的处理-- 这里需要缓存一个当前消息的标志,等到客户端确认收到后再删除这条消息的标志

                    }else{
                        //发送失败的处理-- 放到重发队列中
                    }
                }
            });
            return;
        }
        //不在线 --存到离线消息中
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
     * 回复客户端成功的应答发送方法
     * @param channel
     * @param type
     * @param callBack
     */
    public static void sendOkACK(Channel channel,int type,CallBackAction callBack){
        SimpleProtocol protocol = new SimpleProtocol(type, ChannelConst.S2C_SUCCESS);
        ChannelFuture cf = baseSend(channel,protocol);
        cf.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture result) throws Exception {
                if(callBack==null)return;
                //直接交给回调函数处理
                callBack.callBack(result.isSuccess(),channel,protocol);
            }
        });
    }
}

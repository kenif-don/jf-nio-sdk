package com.villa.im.process;

import com.villa.im.manager.ProtocolManager;
import com.villa.im.model.Protocol;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑处理器的实现类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class DefaultLogicProcessImpl implements LogicProcess {
    /**
     * demo实现  将原目标进行返回
     * 真实场景应该是根据protocol中dataContent获取聊天类型 好友聊天就是to 群聊还需要再去获取群成员
     * @param protocol 消息体
     */
    public List<String> getTargets(Protocol protocol) {
        List<String> list = new ArrayList<>();
        list.add(protocol.getTo());
        return list;
    }
    /**
     * demo实现 将消息记录存到数据库
     */
    public void addMessage(Protocol protocol) {}
    public boolean loginBefore(Channel channel, Protocol protocol) {
//        Log.log("【客户端登录】："+ JSON.toJSONString(protocol));
        return true;
    }
    public boolean logoutBefore(Channel channel, Protocol protocol) {
//        Log.log("【客户端登出】："+ JSON.toJSONString(protocol));
        return true;
    }
    public boolean sendMsgBefore(Channel channel, Protocol protocol) {
//        Log.log("【客户端消息】："+ JSON.toJSONString(protocol));
        return true;
    }
    public void sendFailCallBack(String channelId,Protocol protocol) {
//        Log.log("【客户端消息转发失败】："+JSON.toJSONString(protocol));
    }
    public void sendSuccessCallBack(Protocol protocol) {

//        Log.log("【客户端消息转发成功】："+JSON.toJSONString(protocol));
    }
    public void sendCallBack(Object protocol) {

//        Log.log("【客户端已转发消息,但结果未知】："+JSON.toJSONString(protocol));
    }
    public void customProtocolHandler(Channel channel, Protocol protocol) {
        ProtocolManager.sendMsg(channel,protocol);
    }
}

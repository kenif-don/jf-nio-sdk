package com.jf.im.process;

import com.alibaba.fastjson2.JSON;
import com.jf.im.manager.ProtocolManager;
import com.jf.im.model.LoginInfo;
import com.jf.im.model.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**逻辑业务类*/
public interface LogicProcess {
    /**
     * 新连接进入
     * @param ctx           通道
     * @param channelCount  当前连接数
     */
    default void newChannelAdded(ChannelHandlerContext ctx,int channelCount){}
    /**
     * type-1 单聊 type-8 群聊
     * 根据to获取要转发的目标群体
     * 得到的结果可以是好友id/获取在同一个群的所有用户id
     *
     * --demo实现  将原目标进行返回
     *   真实场景应该是根据protocol中dataContent获取聊天类型 好友聊天就是to 群聊还需要再去获取群成员
     *   @param protocol 消息体
     */
    default List<String> getTargets(Protocol protocol){
        List<String> list = new ArrayList<>();
        list.add(protocol.getTo());
        return list;
    }
    /**
     * 将消息存到数据库
     * ---demo实现 不做任何处理
     */
    default void addMessage(Protocol protocol){}

    /**
     * 长连接的登录请求前置方法,如果返回false 则不会执行长连接登录功能
     * 可以用来做账号密码验证,签名验证等
     *
     * ---demo实现 直接返回登录验证成功
     */
    default boolean loginBefore(Channel channel,Protocol protocol,LoginInfo loginInfo){
        return true;
    }

    /**
     * 登录成功的后置方法 可以作为一般事件
     *
     * ---demo实现 将MQ需要的基本数据放进这里
     */
    default void loginAfter(Channel channel,Protocol protocol,LoginInfo loginInfo) {
    }

    /**
     * 获取登录信息 由业务层解析并返回登录信息
     * ---demo实现 默认在protocol模型的data属性为loginInfo的json对象串
     */
    default LoginInfo getLoginInfo(Channel channel, Protocol protocol){
        return JSON.parseObject(protocol.getData(), LoginInfo.class);
    }
    /**
     * 长连接的退出登录请求前置方法,如果返回false 则不会执行长连接登出功能
     * 可以用来做销毁登录session或者token等操作
     * 当然也可以将退出登录请求独立到http接口,闲置此方法即可 但需要返回true 让长连接登出功能得以继续
     * 如果需要抛错到长连接  调用#ProtocolManager.sendAck(Channel,dataContent,type-ChannelConst中的消息协议类型);
     *
     * ---demo实现 直接返回true 让长连接处理退出操作
     */
    default boolean logoutBefore(Channel channel,Protocol protocol){
        return true;
    }

    /**
     * 服务器接收到通用消息的前置方法 返回false 则直接阻断长连接后续功能 比如转发消息/qos/回调  都不再执行 直接砍断
     * 默认返回true 可用作拉黑关系好友验证，群消息验证操作 比如拉黑不能发送消息等操作
     */
    default boolean sendMsgBefore(Channel channel,Protocol protocol){
        return true;
    }

    /**
     * TODO... 这里需要做到一个用户多个客户端的情况,需要全部失败才算失败,目前仅做到一个失败就回调一次,需要迭代
     * qos失败的回调
     * 这个方法是真正意义上的失败,也就是对方离线或qos过程中离线
     * 此方法适合用来做离线消息
     * 每条消息,每个接收者只触发一次
     * @param channelId 客户端唯一标志,这个标志可用于记录当前离线消息属于谁的
     * @param protocol 消息体
     */
    default void sendFailCallBack(String channelId,Protocol protocol){}

    /**
     * 消息真正意义上的发送成功回调 目标客户端收到并回执给服务器算真正意义的成功
     * 每条消息,每个接收者只触发一次
     */
    default void sendSuccessCallBack(Protocol protocol){}
    /**
     * 发送数据的回调
     * 发送了一次数据的回调(成功则成功,失败则会走qos),并不意味着成功或者失败。这里仅代表发送了,但是客户端是否已收到并不知道,需要在#sendFailCallBack和#sendSuccessCallBack中才能知道
     * 每条消息,每发送一次(在qos中的消息可能会发送多次)就调用此方法一次
     * TODO...udp协议无法监听成功与失败(虽然这里无需成功和失败的状态,但是还需要再次测试udp是否能监听到发送成功)
     */
    default void sendCallBack(Object protocol){}
    /**
     * 自定义协议类型的处理方法 可扩展框架协议外的其他自定义类型协议
     * ---demo实现 直接转发
     * @param channel 客户端消息通道
     * @param protocol 消息体
     */
    default void customProtocolHandler(Channel channel,Protocol protocol){
        ProtocolManager.handlerMsg(channel,protocol);
    }

    /**
     * 客户端掉线 框架会自动T掉这个链接用户 并关闭链接
     */
    default void sessionClosed(String channelId, Channel channel){}
}

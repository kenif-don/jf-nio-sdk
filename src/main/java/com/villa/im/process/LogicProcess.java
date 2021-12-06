package com.villa.im.process;

import com.villa.im.model.Protocol;
import io.netty.channel.Channel;

import java.util.List;

/**
 * 逻辑业务类
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public interface LogicProcess {
    /**
     * 根据toId获取要转发的目标群体
     * 得到的结果可以是好友id/获取在同一个群的所有用户id
     * 如果返回时大于1个目标 将优先采用udp协议传输  协议优先级为 udp>tco>ws
     * 如果这条消息不应该发送 那么请在实现中返回0的数字字符串
     */
    List<String> getTargets(Protocol protocol);

    /**
     * 将消息存到数据库
     */
    void addMessage(Protocol protocol);

    /**
     * 长连接的登录请求前置方法,如果返回false 则不会执行长连接登录功能
     * 可以用来做账号密码验证,签名验证等
     * 当然也可以将登录请求独立到http接口去,这也是本框架推荐的做法,而这个方法需要做的仅仅是验签即可
     */
    boolean loginBefore(Channel channel,Protocol protocol);

    /**
     * 长连接的退出登录请求前置方法,如果返回false 则不会执行长连接登出功能
     * 可以用来做销毁登录session或者token等操作
     * 当然也可以将退出登录请求独立到http接口,闲置此方法即可 但需要返回true 让长连接登出功能得以继续
     * 如果需要抛错到长连接  调用#ProtocolManager.sendAck(Channel,dataContent,type-ChannelConst中的消息协议类型);
     */
    boolean logoutBefore(Channel channel,Protocol protocol);

    /**
     * 服务器接收到通用消息的前置方法 返回false 则直接阻断长连接后续功能 比如转发消息/qos/回调  都不再执行 直接砍断
     * 目前未想到此方法的作用，留作一些个性化业务吧 默认返回true即可
     */
    boolean sendMsgBefore(Channel channel,Protocol protocol);

    /**
     * qos失败的回调
     * 这个方法是真正意义上的失败,也就是对方离线或qos过程中离线 如果对方在线qos会每N秒推送一次消息,直到成功或用户下线为止,并不是真正的失败
     * 此方法适合用来做离线消息
     * 每条消息,每个接收者支触发一次
     */
    void sendFailCallBack(Protocol protocol);

    /**
     * 消息真正意义上的发送成功回调 目标客户端收到并回执给服务器算真正意义的成功
     * 每条消息,每个接收者支触发一次
     */
    void sendSuccessCallBack(Protocol protocol);
    /**
     * 发送数据的回调
     * 发送了一次数据的回调(成功则成功,失败则会走qos),并不意味着成功或者失败。
     * 每条消息,每个接收者只触发一次
     * TODO...udp协议无法监听成功与失败(虽然这里无需成功和失败的状态,但是还需要再次测试udp是否能监听到发送成功)
     */
    void sendCallBack(Object protocol);
    /**
     * 自定义协议类型的处理方法 可扩展框架协议外的其他自定义类型协议
     * @param channel 客户端消息通道
     * @param protocol 消息体
     */
    void customProtocolHandler(Channel channel,Protocol protocol);
}

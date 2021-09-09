package com.villa.im.protocol;

import io.netty.channel.Channel;

/**
 * 申明一个接口 在发送数据的时候作为一个回调函数存在 使用时直接使用匿名内部类对象做回调处理
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public interface CallBackAction {
    /**
     * 发送数据的回调
     * @param success 数据是否发送成功
     * @param channel 发送数据的客户端
     * @param param   发送的数据
     */
    void callBack(boolean success, Channel channel,Object protocol);
}

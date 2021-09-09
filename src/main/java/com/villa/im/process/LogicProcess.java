package com.villa.im.process;

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
    List<String> getTargets(String toId);
}

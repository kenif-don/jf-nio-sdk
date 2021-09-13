package com.villa.im.process;

import com.villa.im.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class DefaultLogicProcessImpl implements LogicProcess {
    /**
     * demo实现  将原目标进行返回
     */
    public List<String> getTargets(String toId) {
        List<String> list = new ArrayList<>();
        list.add(toId);
        return list;
    }

    @Override
    public void addMessage(Protocol protocol) {

    }
}

package com.villa.test;

import com.villa.im.Server;
import com.villa.im.process.DefaultLogicProcessImpl;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Test {
    public static void main(String[] args) {
        //1. 启动协议服务
        Server.getInstance().startupAll(4001,4002,4003);
        //2. 填充业务处理器
        Server.getInstance().initLogicProcess(new DefaultLogicProcessImpl());
        /**
         *  {type:0,from:001}
         *  {type:1,from:001,to:002,data:123,msg_no:001}
         */
    }
}

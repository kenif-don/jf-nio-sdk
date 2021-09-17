package com.villa.test;

import com.villa.im.Server;
import com.villa.im.model.DataProtoType;
import com.villa.im.process.DefaultLogicProcessImpl;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class Test {
    public static void main(String[] args) {
        //0. 如果不手动设置消息协议类型 默认使用json传输 可以手动设置为protobuf协议进行消息数据传输
        Server.getInstance().initDataProtoType(DataProtoType.PROTOBUF);
        //1. 填充业务处理器
        Server.getInstance().initLogicProcess(new DefaultLogicProcessImpl());
        //2. 启动协议服务
        Server.getInstance().startupAll(4001,4002,4003);
        /**
         *  {type:0,from:001}
         *  {type:1,from:001,to:002,data:123,msg_no:001}
         */
    }
}

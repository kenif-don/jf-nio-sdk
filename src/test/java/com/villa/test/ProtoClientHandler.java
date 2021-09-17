package com.villa.test;

import com.villa.im.model.ProtoBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class ProtoClientHandler extends SimpleChannelInboundHandler<ProtoBuf.proto_my> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoBuf.proto_my msg) throws Exception {
        System.out.println(msg.getFrom());
        System.out.println(msg.getType());
        System.out.println(msg.getTo());
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接");
        ProtoBuf.proto_my user = ProtoBuf.proto_my.newBuilder()
                .setType(0).setFrom("002").setTo("001").setDataContent("123").setMsgNo("001").setAck(-1).build();
        ctx.channel().writeAndFlush(user);
    }
}

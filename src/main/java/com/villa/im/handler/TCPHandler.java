package com.villa.im.handler;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoType;
import com.villa.im.protocol.Protocol;
import com.villa.im.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class TCPHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private CoreHandler coreHandler;
    public TCPHandler(CoreHandler coreHandler){
        this.coreHandler = coreHandler;
    }
    /**
     * 链接发送异常
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        coreHandler.exceptionCaught(ctx,cause);
    }
    /**
     * 新链接加入
     */
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().attr(ChannelConst.PROTO_TYPE).set(ProtoType.TCP);
        coreHandler.handlerAdded(ctx);
    }

    /**
     * 断开链接
     */
    public void channelInactive(ChannelHandlerContext ctx) {
        coreHandler.handlerRemoved(ctx);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        String proto = Util.byteBuf2String(buf);
        if(Util.isNotEmpty(proto)){
            coreHandler.channelRead0(ctx,JSON.parseObject(proto, Protocol.class));
        }
    }
}

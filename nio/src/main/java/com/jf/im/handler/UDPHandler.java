package com.jf.im.handler;

import com.jf.im.model.ProtoType;
import com.jf.im.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.timeout.IdleStateEvent;

public class UDPHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private CoreHandler coreHandler;
    public UDPHandler(CoreHandler coreHandler){
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
        Util.putChannelProtoType(ctx.channel(), ProtoType.UDP);
        coreHandler.handlerAdded(ctx);
    }

    /**
     * 断开链接
     */
    public void channelInactive(ChannelHandlerContext ctx) {
        coreHandler.handlerRemoved(ctx);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket content) throws Exception {
        Util.channelRead(ctx,coreHandler,content.content());
    }
    /** 客户端超时 */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            Util.userEventTriggered(ctx,evt,coreHandler);
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}
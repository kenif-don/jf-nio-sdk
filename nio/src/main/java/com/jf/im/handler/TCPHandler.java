package com.jf.im.handler;

import com.jf.im.model.ProtoType;
import com.jf.im.util.NioUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class TCPHandler extends SimpleChannelInboundHandler<ByteBuf>{
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
        NioUtil.putChannelProtoType(ctx.channel(), ProtoType.TCP);
        coreHandler.handlerAdded(ctx);
    }

    /**
     * 断开链接
     */
    public void channelInactive(ChannelHandlerContext ctx) {
        coreHandler.handlerRemoved(ctx);
    }
    /** 收到客户端消息 */
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf content) throws Exception {
        NioUtil.channelRead(ctx,coreHandler,content);
    }
    /** 客户端超时 */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            NioUtil.userEventTriggered(ctx,evt,coreHandler);
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}

package com.villa.im.handler;

import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoType;
import com.villa.im.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
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
        ctx.channel().attr(ChannelConst.PROTO_TYPE).set(ProtoType.TCP);
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
        Util.channelRead(ctx,coreHandler,content);
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

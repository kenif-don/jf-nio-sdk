package com.jf.im.handler;

import com.jf.im.model.ProtoType;
import com.jf.im.util.NioUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private CoreHandler coreHandler;
    public WebSocketHandler(CoreHandler coreHandler){
        this.coreHandler = coreHandler;
    }

    /**链接发送异常*/
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        coreHandler.exceptionCaught(ctx,cause);
    }
    /**新链接加入*/
    public void channelActive(ChannelHandlerContext ctx) {
        NioUtil.putChannelProtoType(ctx.channel(), ProtoType.WS);
        coreHandler.handlerAdded(ctx);
    }

    /**断开链接*/
    public void channelInactive(ChannelHandlerContext ctx) {
        coreHandler.handlerRemoved(ctx);
    }
    /**ws收到消息 将消息(转换为json)传递给CoreHandler处理*/
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame content) {
        NioUtil.channelRead(ctx,coreHandler,content.content());
    }
    /** 客户端事件监听 */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            NioUtil.userEventTriggered(ctx);
        }
    }
}
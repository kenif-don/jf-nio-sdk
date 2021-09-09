package com.villa.im.handler;

import com.alibaba.fastjson.JSON;
import com.villa.im.model.ChannelConst;
import com.villa.im.model.ProtoType;
import com.villa.im.protocol.Protocol;
import com.villa.im.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @作者 微笑い一刀
 * @bbs_url https://blog.csdn.net/u012169821
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private CoreHandler coreHandler;
    public WebSocketHandler(CoreHandler coreHandler){
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
        ctx.channel().attr(ChannelConst.PROTO_TYPE).set(ProtoType.WS);
        coreHandler.handlerAdded(ctx);
    }

    /**
     * 断开链接
     */
    public void channelInactive(ChannelHandlerContext ctx) {
        coreHandler.handlerRemoved(ctx);
    }
    /**
     * ws收到消息 将消息(转换为json)传递给CoreHandler处理
     */
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if(frame instanceof TextWebSocketFrame){
            String proto = ((TextWebSocketFrame)frame).text();
            if(Util.isNotEmpty(proto)){
                coreHandler.channelRead0(ctx,JSON.parseObject(proto, Protocol.class));
            }
        }
    }
}

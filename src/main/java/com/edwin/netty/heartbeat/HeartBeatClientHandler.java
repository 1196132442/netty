package com.edwin.netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author 书生
 */
public class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println(" ====== > [client] message received : " + s);
        if ("idle close".equals(s)) {
            System.out.println(" 服务端关闭连接，客户端也关闭");
            channelHandlerContext.channel().close();
        }
    }
}

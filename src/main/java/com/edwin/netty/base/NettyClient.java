package com.edwin.netty.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author 书生
 */
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            // 创建客户端启动对象
            // 注意客户端使用的不是ServerBootstrap而是Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            // 设置相关参数
            bootstrap.group(clientGroup)
                    // 使用NioSocketChannel作为客户端的通道实现
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            // 加入处理器
                            channel.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            System.out.println("netty client start。。");
            // 启动客户端去连接服务器端
            ChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 9000)).sync();
            // 对通道关闭进行监听
            future.channel().closeFuture().sync();
        }finally {
            clientGroup.shutdownGracefully();
        }
    }


}

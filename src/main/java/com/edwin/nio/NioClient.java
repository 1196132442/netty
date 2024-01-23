package com.edwin.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author 书生
 */
public class NioClient {

    /**
     * 通道管理器
     */
    public Selector selector;

    public static void main(String[] args) throws IOException {
        NioClient client = new NioClient();
        client.initClient("127.0.0.1", 9000);
        client.connect();
    }



    /**
     * 获得一个 Socket 通道，并对该通道做一些初始化的工作
     * @param ip 连接的服务器的 ip
     * @param port 连接的服务器的端口号
     */
    public void initClient(String ip, int port) throws IOException {
        // 获得一个Socket通道
        SocketChannel channel = SocketChannel.open();
        // 设置通道为非阻塞
        channel.configureBlocking(false);
        // 获得一个通道管理器
        selector = Selector.open();

        // 客户端连接服务器,需要在listen（）方法中调用channel.finishConnect() 才能完成连接
        channel.connect(new InetSocketAddress(ip, port));
        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     */
    public void connect() throws IOException {
        // 轮询访问selector
        while (true) {
            selector.select(1000);
            // 获得selector中选中的项的迭代器
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            if (it.hasNext()) {
                SelectionKey key = it.next();
                // 删除已选的key,以防重复处理
                it.remove();
                // 连接事件发生
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 如果正在连接，则完成连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    // 设置成非阻塞
                    channel.configureBlocking(false);
                    // 在这里可以给服务端发送信息哦
                    ByteBuffer buffer = ByteBuffer.wrap("HelloServer".getBytes());
                    channel.write(buffer);
                    // 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
                    channel.register(selector, SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    public void read(SelectionKey key) throws IOException {
        // 服务器可读取消息:得到事件发生的 Socket 通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int len = channel.read(buffer);
        if (len != -1) {
            // 将缓冲区当前的 limit 设置为 position，position 设置为 0，用于后续对缓冲区的读取操作
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println("接收到消息：" + new String(bytes));
        }
    }

}

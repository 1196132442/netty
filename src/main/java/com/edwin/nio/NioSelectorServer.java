package com.edwin.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 书生
 */
public class NioSelectorServer {

    public static Selector selector;

    public static void main(String[] args) throws IOException {

        //创建NIO ServerSocketChannel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(9000));
        //设置ServerSocketChannel为非阻塞
        serverSocket.configureBlocking(false);
        //打开Selector处理Channel，即创建epoll
        selector = Selector.open();
        //把ServerSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功");


        while (true) {
            //阻塞等待需要处理的事件发生
            selector.select(1000);

            //获取 selector 中注册的全部事件的 SelectionKey 实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            //遍历SelectionKey对事件进行处理
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                /*我们必须首先将处理过的 SelectionKey 从选定的键集合中删除。
                    如果我们没有删除处理过的键，那么它仍然会在主集合中以一个激活
                    的键出现，这会导致我们尝试再次处理它。*/
                iterator.remove();
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        //如果是OP_ACCEPT事件，则进行连接获取和事件注册
                        System.out.println("有客户端连接事件发生了。。");
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = server.accept();
                        socketChannel.configureBlocking(false);
                        //这里只注册了读事件，如果需要给客户端发送数据可以注册写事件
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("客户端连接成功");
                    } else if (key.isReadable()) {
                        System.out.println("有客户端数据可读事件发生了。。");
                        //如果是OP_READ事件，则进行读取和打印
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                        int len = socketChannel.read(byteBuffer);
                        //如果有数据，把数据打印出来
                        if (len > 0) {
                            // 将缓冲区当前的 limit 设置为 position，position 设置为 0，用于后续对缓冲区的读取操作
                            byteBuffer.flip();
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            byteBuffer.get(bytes);
                            System.out.println("接收到消息：" + new String(bytes, StandardCharsets.UTF_8));
                            //回写数据
                            byte[] response = "Hello Client".getBytes(StandardCharsets.UTF_8);
                            ByteBuffer res = ByteBuffer.allocate(response.length);
                            res.put(response);
                            res.flip();
                            socketChannel.write(res);
                        } else if (len == -1) {
                            //取消特定的注册关系
                            key.cancel();
                            //如果客户端断开连接，关闭Socket
                            System.out.println("客户端断开连接");
                            socketChannel.close();
                        }
                    }
                }
            }
        }
    }


}

package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Server {

    private final Charset charset = StandardCharsets.UTF_8;

    public void init() throws IOException {

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);

        serverSocketChannel.bind(inetSocketAddress);

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("The server is ready to receive request...");

        while (selector.select() > 0) {

            for(SelectionKey key : selector.selectedKeys()) {
                if(key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                if(key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    StringBuilder stringBuilder = new StringBuilder();
                    try {
                        while (socketChannel.read(byteBuffer) > 0) {
                            byteBuffer.flip();
                            stringBuilder.append(charset.decode(byteBuffer));
                        }
                        System.out.println("received data > " + stringBuilder);
                    } catch (IOException e) {
                        key.cancel();
                    }
                    String content = stringBuilder.toString();
                    if(content.length() > 0) {
                        if(content.startsWith("0")) {
                            String userName = content.substring(1);
                            key.attach(userName);
                        }
                        if(content.startsWith("1")) {
                            content = content.substring(1);
                            for(SelectionKey sk : selector.keys()) {
                                //filter himself
                                String sender = (String)key.attachment();
                                String receiver = (String)sk.attachment();
                                if(sender.equals(receiver)) {
                                    continue;
                                }
                                //filter the ServerSocketChannel
                                Channel channel = sk.channel();
                                if(channel instanceof SocketChannel) {
                                    SocketChannel sc = (SocketChannel)channel;
                                    sc.write(charset.encode(sender + " says to everyone : " + content));
                                }
                            }
                        } else if(content.startsWith("2")) {
                            int receiverLen = Integer.parseInt(content.substring(1, 3));
                            String sender = (String)key.attachment();
                            String receiver = content.substring(3, 3 + receiverLen);
                            String msg = content.substring(3 + receiverLen);

                            for(SelectionKey sk : selector.keys()) {
                                String name = (String)sk.attachment();
                                if(receiver.equals(name)) {
                                    Channel channel = sk.channel();
                                    SocketChannel sc = (SocketChannel)channel;
                                    sc.write(charset.encode(sender + " says to you : " + msg));
                                }
                            }
                        } else if(content.startsWith("3")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("-------\n");
                            for(SelectionKey sk : selector.keys()) {
                                if(sk.channel() instanceof SocketChannel) {
                                    String name = (String)sk.attachment();
                                    sb.append("* ").append(name).append("\n");
                                }
                            }
                            Channel channel = key.channel();
                            SocketChannel sc = (SocketChannel)channel;
                            sc.write(charset.encode(sb.toString()));
                        }
                    }
                }
                selector.selectedKeys().remove(key);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        new Server().init();
    }
}

package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetworkThread extends Thread {
    private Selector selector;
    private static SocketChannel socketChannel;
    private final Charset charset = StandardCharsets.UTF_8;

    public void init() throws IOException {
        selector = Selector.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
        socketChannel = SocketChannel.open(inetSocketAddress);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        Client.countDownLatch.countDown();
    }

    @Override
    public void run() {
        try {
            init();
            System.out.println("Successfully connected.\n");
            while (!Client.stop) {
                selector.select(500);
                for(SelectionKey key : selector.selectedKeys()) {
                    if(key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel)key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        StringBuilder content = new StringBuilder();
                        while (socketChannel.read(byteBuffer) > 0) {
                            byteBuffer.flip();
                            content.append(charset.decode(byteBuffer));
                        }
                        System.out.println("received data>" + content);
                        key.interestOps(SelectionKey.OP_READ);
                    }

                    //need to remove manually
                    selector.selectedKeys().remove(key);
                }
            }
            if(selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SocketChannel getSocketChannel() {
        return socketChannel;
    }
}
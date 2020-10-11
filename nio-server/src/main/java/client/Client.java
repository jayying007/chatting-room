package client;

import java.util.concurrent.CountDownLatch;

public class Client {

    public static CountDownLatch countDownLatch = new CountDownLatch(1);
    public static volatile boolean stop = false;

    public static void main(String[] args) {
        new NetworkThread().start();
        new InteractiveThread().start();
    }

}

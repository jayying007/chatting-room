package client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InteractiveThread extends Thread {

    public static String userName;
    private final Charset charset = StandardCharsets.UTF_8;
    private final Scanner scanner = new Scanner(System.in);


    @Override
    public void run() {
        try {
            Client.countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.print("Please input your name>");
        userName = scanner.nextLine();
        send("0" + userName);

        showMenu();
        while (scanner.hasNext()) {
            String op = scanner.nextLine();
            switch (op) {
                case "1": {
                    System.out.print("content>");
                    String msg = scanner.nextLine();

                    send("1" + msg);
                    break;
                }
                case "2": {
                    System.out.print("the receiver>");
                    String receiver = scanner.nextLine();
                    int len = receiver.length();
                    System.out.print("content>");
                    String msg = scanner.nextLine();
                    send(String.format("2%s%s%s", len > 9 ? len+"" : "0"+len, receiver, msg));
                    break;
                }
                case "3": {
                    send("3");
                    break;
                }
                case "4": {
                    Client.stop = true;
                    System.exit(0);
                }
                default: {
                    System.out.println("invalid command!");
                }
            }
        }
    }

    public static void showMenu() {
        System.out.println("\n========Welcome," + userName + "========");
        System.out.println("1. Talk public");
        System.out.println("2. Talk to someone");
        System.out.println("3. Show online person");
        System.out.println("4. Exit");
        System.out.println("=======Input the command via number=======\n");
    }

    public void send(String msg) {
        try {
            SocketChannel socketChannel = NetworkThread.getSocketChannel();
            socketChannel.write(charset.encode(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

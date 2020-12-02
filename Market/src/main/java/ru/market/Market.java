package ru.market;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Market {

    static final int PORT = 5001;
    static final String ADDRESS = "localhost";
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    private void run() throws Exception {
        System.out.println("Start!");

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT); //здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

//        new Thread(() -> {
//            Scanner scanner = new Scanner(System.in);
//            while (true) {
//                String line = scanner.nextLine();
//                if ("q".equals(line)) {
//                    System.exit(0);
//                }
//                try {
//                    queue.put(line);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                SelectionKey key = channel.keyFor(selector);
//                key.interestOps(SelectionKey.OP_WRITE);
//                selector.wakeup();
//            }
//        }).start();

        while (true) {
            selector.select();
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isConnectable()) {
                    channel.finishConnect();
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    buffer.clear();
                    channel.read(buffer);
                    System.out.println("Received = " + new String(buffer.array()));
                } else if (selectionKey.isWritable()) {
                    String line = queue.poll();
                    if (line != null) {
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Market().run();
    }
}

package ru.bloker;

import ru.bloker.message.Fix;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class NioClient {

    static final int PORT = 5000;
    static final String ADDRESS = "localhost";
    private ByteBuffer buffer = ByteBuffer.allocate(2048);

    private void run() throws Exception {
        System.out.println("Start!");

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        SelectionKey register = channel.register(selector, SelectionKey.OP_CONNECT);//здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    System.exit(0);
                }
                line += "|10=" + getCheckSum(line);
                System.err.println(line);
                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SelectionKey key = channel.keyFor(selector);
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }).start();

        while (true) {
            selector.select();
            System.err.println("after selector.select()");
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isConnectable()) {
                    System.out.println("selectionKey.isConnectable()");
                    channel.finishConnect();
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    System.out.println("selectionKey.isReadable()");
                    buffer.clear();
                    channel.read(buffer);
                    System.out.println("Received = " + new String(buffer.array()));
                } else if (selectionKey.isWritable()) {
                    System.out.println("selectionKey.isWritable()");
                    String line = queue.poll();
                    if (line != null) {
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private String getCheckSum(String message) {
        byte bytes[] = message.getBytes();

        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);

        // get the current checksum value
        long checksumValue = checksum.getValue();

        return String.valueOf(checksumValue);
    }

    public static void main(String[] args) throws Exception {
        new NioClient().run();
    }
}

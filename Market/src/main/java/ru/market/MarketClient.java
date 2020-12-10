package ru.market;

import ru.market.model.Fix;

import java.io.IOException;
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

public class MarketClient {

    static final int PORT = 5001;
    static final String ADDRESS = "localhost";
    private ByteBuffer buffer = ByteBuffer.allocate(128);
    private String id = null;

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
                StringBuffer stringBuffer = new StringBuffer();
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
                if (line.length() < 24) {
                    id = line;
                    stringBuffer.append(line);
                } else {
                    stringBuffer.append("49=").append(id).append("|").append(line);
                    String temp = stringBuffer.toString();
                    stringBuffer.append("|10=").append(getCheckSum(temp));
                    System.err.println(stringBuffer);
                }
                try {
                    queue.put(stringBuffer.toString());
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
                    int numRead = channel.read(buffer);

                    if (id == null) {
                        id = new String(buffer.array()).trim();
                    }
                    System.out.println("Received = " + new String(buffer.array()));
                    Thread.sleep(10000);
                    try {
                        Fix fix = new Fix(buffer.array(), numRead);
                        fix.setDealType("3");
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(fix.toString()).append("|")
                                .append(StringUtil.CHECK_SUM).append("=").append(getCheckSum(fix.toString()));
                        channel.write(ByteBuffer.wrap(stringBuffer.toString().getBytes()));
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                } else if (selectionKey.isWritable()) {
                    System.out.println("selectionKey.isWritable()");
                    String line = queue.poll();
                    if (line != null) {
                        channel.write(ByteBuffer.wrap(line.getBytes()));
                    }
//                    if ("49=100000|54=1|1=bax|15=100|38=11|56=100000|10=304290585".equals(line)) {
//                        try {
//                            channel.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        System.exit(0);
//                    }
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
        new MarketClient().run();
    }
}

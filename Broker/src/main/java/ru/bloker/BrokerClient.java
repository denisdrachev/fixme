package ru.bloker;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Slf4j
public class BrokerClient {

    static final int PORT = 5000;
    static final String ADDRESS = "localhost";
    private ByteBuffer buffer = ByteBuffer.allocate(128);
    private String id = null;

    private void run() throws Exception {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);//здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(ADDRESS, PORT));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        initConsoleListener(channel, selector, queue);

        while (true) {
            selector.select();
            handle(channel, selector, queue);
            selector.selectedKeys().clear();
        }
    }

    private void handle(SocketChannel channel, Selector selector, BlockingQueue<String> queue) throws IOException {
        for (SelectionKey selectionKey : selector.selectedKeys()) {
            if (selectionKey.isConnectable()) {
                channel.finishConnect();
                log.info("Connection success");
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            } else if (selectionKey.isReadable()) {
                ((Buffer) buffer).clear();
                int numRead = channel.read(buffer);
                String inputString = getString(numRead);
                log.info("Received: {}", inputString);
                if (numRead == 6) {
                    id = inputString;
                    log.info("Set ID: {}", id);
                }
            } else if (selectionKey.isWritable()) {
                String line = queue.poll();
                if (line != null) {
                    log.info("Send: {}", line);
                    channel.write(ByteBuffer.wrap(line.getBytes()));
                }
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private String getString(int numRead) {
        byte[] dataCopy = new byte[numRead];
        System.arraycopy(buffer.array(), 0, dataCopy, 0, numRead);
        return new String(dataCopy);
    }

    private void initConsoleListener(SocketChannel channel, Selector selector, BlockingQueue<String> queue) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();

                consoleHandle(channel, queue, line);

                SelectionKey key = channel.keyFor(selector);
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }).start();
    }

    private void consoleHandle(SocketChannel channel, BlockingQueue<String> queue, String line) {
        StringBuffer stringBuffer = new StringBuffer();
        if ("q".equals(line)) {
            try {
                channel.close();
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
            System.exit(0);
        }
        if (line.length() == 6) {
            try {
                Integer.parseInt(line);
                stringBuffer.append(line);
            } catch (Exception e) {
                log.info("Incorrect input");
            }
        } else {
            stringBuffer.append("49=").append(id).append("|").append(line);
            String temp = stringBuffer.toString();
            stringBuffer.append("|10=").append(getCheckSum(temp));
        }
        try {
            if (stringBuffer.length() > 0)
                queue.put(stringBuffer.toString());
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }

    private String getCheckSum(String message) {
        byte bytes[] = message.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        long checksumValue = checksum.getValue();
        return String.valueOf(checksumValue);
    }

    public static void main(String[] args) throws Exception {
        new BrokerClient().run();
    }
}

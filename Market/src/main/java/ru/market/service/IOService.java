package ru.market.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class IOService {

    private final ConnectorService connector;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    SocketChannel channel;
    Selector selector;

    public IOService(ConnectorService connector) {
        this.connector = connector;
        channel = connector.getChannel();
        selector = connector.getSelector();
        new Thread(this::consoleReader).start();
        new Thread(this::worker).start();
    }

    private void consoleReader() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String line = scanner.nextLine();
            if ("q".equals(line)) {
                System.exit(0);
            }
            try {
                queue.put(line);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SelectionKey key = channel.keyFor(selector);
            key.interestOps(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    @SneakyThrows
    private void worker() {
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
            selector.selectedKeys().clear();
        }
    }
}

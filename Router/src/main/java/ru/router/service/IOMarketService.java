package ru.router.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.router.ChangeRequest;
import ru.router.EchoWorker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static ru.router.ChangeRequest.CHANGEOPS;

@Service
public class IOMarketService {

    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private EchoWorker worker = new EchoWorker();
    private final List<ChangeRequest> changeRequests = new LinkedList();
    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

    public IOMarketService(MarketConnectorService connectorService) {
        selector = connectorService.getSelector();
        run();
    }

    @SneakyThrows
    public void run() {
        while (true) {
            synchronized (changeRequests) {
                for (ChangeRequest change : changeRequests) {
                    switch (change.type) {
                        case CHANGEOPS:
                            SelectionKey key = change.getChannel().keyFor(selector);
                            key.interestOps(change.getOps());
                            break;
                        default:
                    }
                }
                changeRequests.clear();
            }
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }


    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead = socketChannel.read(readBuffer);
        worker.processData(this, socketChannel, readBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        synchronized (pendingData) {
            List<ByteBuffer> queue = pendingData.get(socketChannel);
            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    break;
                }
                System.out.println("Send echo = " + new String(buf.array()));
                queue.remove(0);
            }
            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    void send(SocketChannel socket, byte[] data) {
        synchronized (changeRequests) {
            changeRequests.add(new ChangeRequest(socket, CHANGEOPS, OP_WRITE));
            synchronized (pendingData) {
                List<ByteBuffer> queue = pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList<>();
                    pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }
        selector.wakeup();
    }
}

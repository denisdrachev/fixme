package ru.router;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_WRITE;
import static ru.router.ChangeRequest.CHANGEOPS;

public class NioServer implements Runnable {
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private EchoWorker worker = new EchoWorker();
    private final List<ChangeRequest> changeRequests = new LinkedList();
    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();
    static final int PORT_BROKER = 5000;
    static final int PORT_MARKET = 5001;
    static final String ADDRESS = "localhost";


    private NioServer(String address, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(address, port);
        serverChannel.socket().bind(isa);
        selector = SelectorProvider.provider().openSelector();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        new Thread(worker).start();
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NioServer(ADDRESS, PORT_BROKER)).start();
//        new Thread(new NioServer(ADDRESS, PORT_MARKET)).start();
        new NioServer(ADDRESS, PORT_MARKET).run();
    }

    @SneakyThrows
    public void run() {
        while (true) {
            synchronized (changeRequests) {
                for (ChangeRequest change : changeRequests) {
                    switch (change.type) {
                        case CHANGEOPS:
                            SelectionKey key = change.channel.keyFor(selector);
                            key.interestOps(change.ops);
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
//package ru.router.service;
//
//import lombok.SneakyThrows;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import ru.router.ChangeRequest;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import static java.nio.channels.SelectionKey.OP_WRITE;
//import static ru.router.ChangeRequest.CHANGEOPS;
//
//@Service
//public class IOBrokerService implements IOInterface, Runnable {
//
//    private IOMarketService marketService;
//    private Selector brokerSelector;
//    private Selector marketSelector;
//    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
//    private final List<ChangeRequest> changeRequests = new LinkedList();
//    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();
//    private final SenderService senderService = new SenderService();
//    ExecutorService executor = Executors.newFixedThreadPool(10);
//
//    private SocketChannel marketChannel;
//
//    @Autowired
//    public IOBrokerService(BrokerConnectorService brokerConnectorService, MarketConnectorService marketConnectorService, IOMarketService marketService /*, SenderService senderService*/) {
//        this.marketService = marketService;
//        marketSelector = marketConnectorService.getSelector();
//        brokerSelector = brokerConnectorService.getSelector();
//        marketChannel = marketConnectorService.getChannel();
//        executor.execute(senderService);
//        executor.execute(this);
//    }
//
//    @Override
//    @SneakyThrows
//    public void run() {
//        while (true) {
//            synchronized (changeRequests) {
//                for (ChangeRequest change : changeRequests) {
//                    switch (change.type) {
//                        case CHANGEOPS:
//                            SelectionKey key = change.getChannel().keyFor(brokerSelector);
//                            key.interestOps(change.getOps());
//                            break;
//                        default:
//                    }
//                }
//                changeRequests.clear();
//            }
//            brokerSelector.select();
//            Iterator<SelectionKey> selectedKeys = brokerSelector.selectedKeys().iterator();
//            while (selectedKeys.hasNext()) {
//                SelectionKey key = selectedKeys.next();
//                selectedKeys.remove();
//                if (!key.isValid()) {
//                    continue;
//                }
//                if (key.isAcceptable()) {
//                    accept(key);
//                } else if (key.isReadable()) {
//                    read(key);
//                } else if (key.isWritable()) {
//                    write(key);
//                }
//            }
//        }
//    }
//
//
//    private void accept(SelectionKey key) throws IOException {
//        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
//        SocketChannel socketChannel = serverSocketChannel.accept();
//        socketChannel.configureBlocking(false);
//        socketChannel.register(brokerSelector, SelectionKey.OP_READ);
//    }
//
//    private void read(SelectionKey key) throws IOException {
//        SocketChannel socketChannel = (SocketChannel) key.channel();
//        readBuffer.clear();
//        int numRead = socketChannel.read(readBuffer);
//        System.out.println("Router income: " + new String(readBuffer.array()));
//        senderService.processData(marketService, marketChannel, readBuffer.array(), numRead);
//    }
//
//    private void write(SelectionKey key) throws IOException {
//        SocketChannel socketChannel = (SocketChannel) key.channel();
//        synchronized (pendingData) {
//            List<ByteBuffer> queue = pendingData.get(socketChannel);
//            while (!queue.isEmpty()) {
//                ByteBuffer buf = queue.get(0);
//                socketChannel.write(buf);
//                if (buf.remaining() > 0) {
//                    break;
//                }
//                System.out.println("IOBrokerService Send echo = " + new String(buf.array()));
//                queue.remove(0);
//            }
//            if (queue.isEmpty()) {
//                key.interestOps(SelectionKey.OP_READ);
//            }
//        }
//    }
//
//    public void send(SocketChannel socket, byte[] data) {
//        synchronized (changeRequests) {
//            changeRequests.add(new ChangeRequest(socket, CHANGEOPS, OP_WRITE));
//            synchronized (pendingData) {
//                List<ByteBuffer> queue = pendingData.get(socket);
//                if (queue == null) {
//                    queue = new ArrayList<>();
//                    pendingData.put(socket, queue);
//                }
//                queue.add(ByteBuffer.wrap(data));
//            }
//        }
//        brokerSelector.wakeup();
//    }
//}

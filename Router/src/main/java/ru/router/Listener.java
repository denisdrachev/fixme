package ru.router;

import lombok.SneakyThrows;
import ru.router.chain.Chain;
import ru.router.model.Fix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ru.router.ChangeRequest.CHANGEOPS;

public class Listener implements Runnable {

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final List<ChangeRequest> changeRequests = new LinkedList();
    private Selector selector;
    private Chain chain;
    private TransactionRecovery transactionRecovery;


    public Listener(Selector selector, Chain chain, TransactionRecovery transactionRecovery) {
        this.selector = selector;
        this.chain = chain;
        this.transactionRecovery = transactionRecovery;

//        Transactor transactor = new Transactor();
//        Sider sider = new Sider();
//        Sender sender = new Sender();
//
//        chain = new Validator();
//        chain.setNext(sider);
//        chain.getNext().setNext(transactor);
//        chain.getNext().getNext().setNext(sender);
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
                    System.out.println("key.isWritable() WOW");
//                    write(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(ByteBuffer.wrap(String.valueOf(Action.index).getBytes()));
        Action.channelMap.put(String.valueOf(Action.index), socketChannel);
        System.out.println("Connection: " + Action.index);
        Action.index++;
    }

    @SneakyThrows
    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead = socketChannel.read(readBuffer);
        if (numRead == -1) {
            Map<String, SocketChannel> channelMap = Action.channelMap;
            int q = 0;

            for (Map.Entry<String, SocketChannel> entry : channelMap.entrySet()) {
                if (entry.getValue() == socketChannel) {
                    System.err.println(entry.getKey() + " disconnected");
                }
            }
            key.cancel();
            key.attach(null);
            System.out.println("Someone disconnected");
            return;
        }
        System.err.println("numRead: " + numRead);
        if (numRead < 44) {
            try {
                byte[] dataCopy = new byte[numRead];
                System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
                String s1 = new String(dataCopy);

                Iterable<Fix> transactionIfNeeded = transactionRecovery.getTransactionIfNeeded(socketChannel, s1);

                for (Fix fix : transactionIfNeeded) {
                    socketChannel.write(ByteBuffer.wrap(fix.toString().getBytes()));
                    System.out.println("Sending success: " + fix);
//                    log.info("Sending success");
                    fix.setStatus(true);
                    transactionRecovery.updateFixMessageTransaction(fix);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                Fix fix = new Fix(readBuffer.array(), numRead);
                System.err.println(fix);
                chain.handle(fix);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

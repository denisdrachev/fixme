package ru.router.active;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.router.chain.Chain;
import ru.router.model.Fix;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

import static ru.router.active.ChangeRequest.CHANGEOPS;

@Slf4j
public class Listener implements Runnable {

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final List<ChangeRequest> changeRequests = new LinkedList();
    private Selector selector;
    private Chain chain;
    private TransactionRecovery transactionRecovery;
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();


    public Listener(Selector selector, Chain chain, TransactionRecovery transactionRecovery) {
        this.selector = selector;
        this.chain = chain;
        this.transactionRecovery = transactionRecovery;
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

            String removeKey = null;

            for (Map.Entry<String, SocketChannel> entry : channelMap.entrySet()) {
                if (entry.getValue() == socketChannel) {
                    System.err.println(entry.getKey() + " disconnected");
                    removeKey = entry.getKey();
                    break;
                }
            }
            if (removeKey != null) {
                channelMap.remove(removeKey);
            }

            key.cancel();
            key.attach(null);
//            System.out.println("Someone disconnected");
            return;
        }
        System.err.println("numRead: " + numRead);
        if (numRead == 6) {
            try {
                byte[] dataCopy = new byte[numRead];
                System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
                String newName = new String(dataCopy);

                String oldName = null;
                for (Map.Entry<String, SocketChannel> entry : Action.channelMap.entrySet()) {
                    if (entry.getValue() == socketChannel) {
                        oldName = entry.getKey();
                        break;
                    }
                }

                if (oldName != null && !Action.channelMap.containsKey(newName)) {
                    Action.channelMap.put(newName, Action.channelMap.get(oldName));
                    Action.channelMap.remove(oldName);
                    log.info("Swap Broker name: old name = {}\tnew name = {}", oldName, newName);

                    Iterable<Fix> transactionIfNeeded = transactionRecovery.getTransactionIfNeeded(socketChannel, newName);

                    for (Fix fix : transactionIfNeeded) {
                        socketChannel.write(ByteBuffer.wrap(fix.toString().getBytes()));
                        System.out.println("Sending success: " + fix);
//                    log.info("Sending success");
                        fix.setStatus(true);
                        transactionRecovery.updateFixMessageTransaction(fix);
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                Fix fix = new Fix(readBuffer.array(), numRead);
                System.err.println(fix);

                Set<ConstraintViolation<Fix>> validate = validator.validate(fix);
                String validatorMessage = validate.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));

                log.info("Validator message: ({}) {}", validatorMessage.length(), validatorMessage);
                if (validatorMessage.length() == 0) {
                    chain.handle(fix);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

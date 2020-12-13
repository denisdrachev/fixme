package ru.router.active;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.router.chain.Chain;
import ru.router.model.Fix;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.router.active.ChangeRequest.CHANGEOPS;
import static ru.router.utils.ValidationUtil.validate;

@Slf4j
public class MarketListener implements Runnable {

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final List<ChangeRequest> changeRequests = new LinkedList();
    private Selector selector;
    private Chain chain;
    private int index = 500000;
    public static Map<String, SocketChannel> channelMap = new ConcurrentHashMap<>();

    public MarketListener(Selector selector, Chain chain) {
        this.selector = selector;
        this.chain = chain;
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
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(ByteBuffer.wrap(String.valueOf(index).getBytes()));
        channelMap.put(String.valueOf(index), socketChannel);
        log.info("Connection Market: {}", index);
        index++;
    }

    @SneakyThrows
    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        readBuffer.clear();
        int numRead = socketChannel.read(readBuffer);
        if (numRead == -1) {
            closeConnection(key, socketChannel);
            return;
        }

        String input = getString(numRead);

        if (numRead == 6) {
            try {
                initNewId(socketChannel, input);
            } catch (Exception e) {
                log.warn("Incorrect input. {}", e.getMessage());
            }
        } else {
            String[] inputSplit = input.split(" ");
            for (String inputString : inputSplit) {
                try {
                    handle(inputString);
                } catch (Exception e) {
                    log.warn("Incorrect input. {}", e.getMessage());
                }
            }
        }
    }

    private void handle(String inputString) {
        Fix fix = new Fix(inputString);
        Set<ConstraintViolation<Fix>> validate = validate(fix);
        String validatorMessage = validate.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.info("Validator message: ({}) {}", validatorMessage.length(), validatorMessage);
        if (validatorMessage.length() == 0) {
            chain.handle(fix);
        }
    }

    private void initNewId(SocketChannel socketChannel, String input) {
        String oldName = null;
        for (Map.Entry<String, SocketChannel> entry : channelMap.entrySet()) {
            if (entry.getValue() == socketChannel) {
                oldName = entry.getKey();
                break;
            }
        }

        if (oldName != null && !channelMap.containsKey(input)) {
            channelMap.put(input, channelMap.get(oldName));
            channelMap.remove(oldName);
            log.info("Swap Market name: old name = {}\tnew name = {}", oldName, input);
        }
    }

    private String getString(int numRead) {
        byte[] dataCopy = new byte[numRead];
        System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
        String input = new String(dataCopy);
        log.info("Received: {}", input);
        return input;
    }

    private void closeConnection(SelectionKey key, SocketChannel socketChannel) {
        String removeKey = null;

        for (Map.Entry<String, SocketChannel> entry : channelMap.entrySet()) {
            if (entry.getValue() == socketChannel) {
                System.err.println("Market " + entry.getKey() + " disconnected");
                removeKey = entry.getKey();
                break;
            }
        }
        if (removeKey != null) {
            channelMap.remove(removeKey);
        }

        key.cancel();
        key.attach(null);
    }
}

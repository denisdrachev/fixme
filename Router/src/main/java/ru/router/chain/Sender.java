package ru.router.chain;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.router.NioServer;
import ru.router.model.Fix;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Data
@Slf4j
public class Sender implements Chain {

    private Chain next = null;

    @Override
    synchronized public void handle(Fix message) {
        initSide(message);
    }

    @SneakyThrows
    private void initSide(Fix message) {
        SocketChannel socketChannel = NioServer.getChannelMap().get(message.getSide());
        log.info("Send to: {} message: {}", message.getSide(), message.toString());
        socketChannel.write(ByteBuffer.wrap(message.getBytes()));
    }
}

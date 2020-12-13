package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.active.BrokerListener;
import ru.router.active.MarketListener;
import ru.router.model.Fix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Data
@Slf4j
public class Sender implements Chain {

    private Chain next = null;

    @Override
    synchronized public void handle(Fix message) {
        send(message);
    }

    private void send(Fix message) {
        SocketChannel socketChannel = null;
        String side = null;

        if ("1".equals(message.getDealType()) || "2".equals(message.getDealType())) {
            socketChannel = MarketListener.channelMap.get(message.getSide());
            side = "Market";
        } else if ("3".equals(message.getDealType()) || "4".equals(message.getDealType())) {
            socketChannel = BrokerListener.channelMap.get(message.getSide());
            side = "Broker";
        }

        try {
            socketChannel.write(ByteBuffer.wrap(message.getBytes()));
            log.info("Send to {}:{} Message:{}", side, message.getSide(), message);
            if (next != null) {
                next.handle(message);
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
}

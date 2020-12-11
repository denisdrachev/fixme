package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.active.Action;
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

//    @SneakyThrows
    private void send(Fix message) {
        SocketChannel socketChannel = Action.channelMap.get(message.getSide());
        try {
            int write = socketChannel.write(ByteBuffer.wrap(message.getBytes()));
            log.info("Send: to:{}\tLength:{}\tMessage:{}", message.getSide(), write, message);
            if (next != null) {
                next.handle(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

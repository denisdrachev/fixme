package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.Action;
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
        initSide(message);
    }

//    @SneakyThrows
    private void initSide(Fix message) {
        SocketChannel socketChannel = Action.channelMap.get(message.getSide());
        log.info("Send to: {} message: {}", message.getSide(), message.toString());
        System.out.println("Sleep 1 second");
//        Thread.sleep(10000);
//        System.out.println("socketChannel.isConnected(): ");
//        System.out.println(socketChannel.isConnected());
//        System.out.println(socketChannel.isConnectionPending());
//        System.out.println(socketChannel.isBlocking());
//        System.out.println(socketChannel.isOpen());
//        System.out.println(socketChannel.isRegistered());
//        System.out.println(socketChannel.finishConnect());
//        Thread.sleep(1000);
        int write = 0;
        try {
            write = socketChannel.write(ByteBuffer.wrap(message.getBytes()));
            System.err.println("write: " + write);
//        if(!socketChannel.isConnected()){
//            System.err.println("!socketChannel.isConnected()");
//            socketChannel.close();
//            return;
//        }

            log.info("Sending success");
//            message.setStatus(true);
            if (next != null) {
                next.handle(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

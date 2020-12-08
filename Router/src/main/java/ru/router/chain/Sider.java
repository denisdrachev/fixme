package ru.router.chain;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.router.NioServer;
import ru.router.model.Fix;

@Data
@Slf4j
public class Sider implements Chain {

    private Chain next = null;

    @Override
    synchronized public void handle(Fix message) {
        if (initSide(message)) {
            next.handle(message);
        }
    }

    private boolean initSide(Fix message) {
        if (message.getDealType().equals("1") || message.getDealType().equals("2")) {
            if (NioServer.getChannelMap().containsKey(message.getMarketId())) {
                message.setSide(message.getMarketId());
                return true;
            }
        } else if (message.getDealType().equals("3") || message.getDealType().equals("4")) {
            if (NioServer.getChannelMap().containsKey(message.getBrokerId())) {
                message.setSide(message.getBrokerId());
            }
        }
        log.info("Invalid side");
        return false;
    }
}

package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.active.Action;
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
        if ("1".equals(message.getDealType()) || "2".equals(message.getDealType())) {
            if (Action.channelMap.containsKey(message.getMarketId())) {
                message.setSide(message.getMarketId());
                return true;
            }
        } else if ("3".equals(message.getDealType()) || "4".equals(message.getDealType())) {
            if (Action.channelMap.containsKey(message.getBrokerId())) {
                message.setSide(message.getBrokerId());
                return true;
            }
        }
        log.info("Invalid side");
        return false;
    }
}

package ru.market.handler;

import ru.market.operations.*;
import ru.market.service.Market;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler {

    private Map<String, Operation> operationMap = new HashMap<>();

    public MessageHandler(Market market) {
        operationMap.put("N", new ConnectOperation(market));
        operationMap.put("L", new LoadTransactionsOperation(market));
        operationMap.put("B", new BuyOperation(market));
        operationMap.put("H", new InstrumentsOperation(market));
    }

    public void handle(String message) {
        String[] split = message.split(" \\| ");
    }
}

package ru.market.operations;

import ru.market.service.Market;

public class BuyOperation implements Operation {

    Market market;

    public BuyOperation(Market market) {
        this.market = market;
    }

    @Override
    public String getResponse(String message) {
        return null;
    }

    //список инструментов
    //ok
    //ne ok
}

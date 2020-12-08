package ru.market.operations;

import ru.market.service.Market;

public class InstrumentsOperation implements Operation {

    Market market;

    public InstrumentsOperation(Market market) {
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

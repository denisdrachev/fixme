package ru.market.service;

import ru.market.model.Instrument;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Market {

//    private static Market marketService;
    private List<Instrument> instruments;

    public Market() {
        init();
    }

//    public static Market getInstance() {
//        if (marketService == null) {
//            marketService = new Market();
//        }
//        return marketService;
//    }


    private void init() {
        instruments = Stream.of(
                new Instrument("dollar", 1000),
                new Instrument("euro", 1000),
                new Instrument("rub", 1000),
                new Instrument("pesso", 1000))
                .collect(Collectors.toList());

    }

}

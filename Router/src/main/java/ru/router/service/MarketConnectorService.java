//package ru.router.service;
//
//import lombok.Getter;
//import lombok.SneakyThrows;
//import org.springframework.stereotype.Service;
//import ru.router.config.ConfigProperties;
////import ru.market.config.ConfigProperties;
//
//import java.net.InetSocketAddress;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.channels.spi.SelectorProvider;
//
//@Getter
//@Service
//public class MarketConnectorService {
//
//
//    private SocketChannel channel;
//    private Selector selector;
//
//    public MarketConnectorService(ConfigProperties properties) {
//        startServer(properties.getMarketAddress(), properties.getMarketPort());
//    }
//
//    @SneakyThrows
//    private void startServer(String address, int port) {
//        ServerSocketChannel serverChannel = ServerSocketChannel.open();
//        serverChannel.configureBlocking(false);
//        InetSocketAddress isa = new InetSocketAddress(address, port);
//        serverChannel.socket().bind(isa);
//        selector = SelectorProvider.provider().openSelector();
//        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
//        System.out.println("Market connector service started");
//    }
//}

package ru.market.service;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.market.config.ConfigProperties;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Getter
@Service
public class ConnectorService {


    private SocketChannel channel;
    private Selector selector;

    @SneakyThrows
    public ConnectorService(ConfigProperties properties) {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT); //здесь объединяются селектор и канал (и вид активности: коннект)
        channel.connect(new InetSocketAddress(properties.getAddress(), properties.getPort()));
    }
}

package ru.router.active;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.router.config.ConfigProperties;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

@Data
@Service
public class BrokerChannel {

    private Selector selector;

    @SneakyThrows
    public BrokerChannel(ConfigProperties properties) {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(properties.getBrokerAddress(), properties.getBrokerPort());
        serverChannel.socket().bind(isa);
        selector = SelectorProvider.provider().openSelector();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
}

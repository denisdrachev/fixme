package ru.router;

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
public class MarketChannel {

    private Selector selector;
//    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
//    private EchoWorker worker = new EchoWorker();
//    private final List<ChangeRequest> changeRequests = new LinkedList();
//    private ExecutorService executor = Executors.newFixedThreadPool(10);

    @SneakyThrows
    public MarketChannel(ConfigProperties properties) {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress isa = new InetSocketAddress(properties.getMarketAddress(), properties.getMarketPort());
        serverChannel.socket().bind(isa);
        selector = SelectorProvider.provider().openSelector();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

//        executor.execute(worker);
    }
}

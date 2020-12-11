package ru.router.active;

import org.springframework.stereotype.Service;
import ru.router.chain.*;
import ru.router.repositories.TransactionRepository;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Action {

    private final BrokerChannel brokerChannel;
    private final MarketChannel marketChannel;

    public static int index = 100000;
    //TODO убрать этот параметр в листнер, переделать поиск для side
    public static Map<String, SocketChannel> channelMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private Chain chain;

    private final TransactionRecovery transactionRecovery;

    public Action(BrokerChannel brokerChannel, MarketChannel marketChannel, TransactionRepository repository, TransactionRecovery transactionRecovery) {
        this.brokerChannel = brokerChannel;
        this.marketChannel = marketChannel;
        this.transactionRecovery = transactionRecovery;


        Transactor transactor = new Transactor();
        transactor.setRepository(repository);
        Sider sider = new Sider();
        Sender sender = new Sender();
        Transactor transactorAfter = new Transactor();
        transactorAfter.setRepository(repository);

        chain = new Validator();
        chain.setNext(sider);
        chain.getNext().setNext(transactor);
        chain.getNext().getNext().setNext(sender);
//        chain.getNext().getNext().getNext().setNext(transactorAfter);

        Listener brokerListener = new Listener(brokerChannel.getSelector(), chain, transactionRecovery);
        Listener marketListener = new Listener(marketChannel.getSelector(), chain, transactionRecovery);

        executor.execute(brokerListener);
        executor.execute(marketListener);
    }
}

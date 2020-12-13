package ru.router.active;

import org.springframework.stereotype.Service;
import ru.router.chain.*;
import ru.router.repository.TransactionRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Action {

    private final BrokerChannel brokerChannel;
    private final MarketChannel marketChannel;

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private Chain chain;

    private final TransactionRecovery transactionRecovery;

    public Action(BrokerChannel brokerChannel, MarketChannel marketChannel, TransactionRepository repository,
                  TransactionRecovery transactionRecovery) {
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

        BrokerListener brokerListener = new BrokerListener(brokerChannel.getSelector(), chain, transactionRecovery);
        MarketListener marketListener = new MarketListener(marketChannel.getSelector(), chain);

        executor.execute(brokerListener);
        executor.execute(marketListener);
    }
}

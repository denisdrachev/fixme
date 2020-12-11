package ru.router.active;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.router.model.Fix;
import ru.router.repositories.TransactionRepository;

import java.nio.channels.SocketChannel;
import java.util.List;

@Data
@Slf4j
@Service
@AllArgsConstructor
public class TransactionRecovery {

    private TransactionRepository repository;

    public Iterable<Fix> getTransactionIfNeeded(SocketChannel socketChannel, String id) {
        return repository.findByBrokerIdAndStatusOrderByTime(id, false);
    }

    public void updateFixMessageTransaction(Fix message) {
        repository.save(message);
    }
}

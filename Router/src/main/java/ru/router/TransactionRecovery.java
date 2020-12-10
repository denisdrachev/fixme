package ru.router;

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

//    public void sendTransactions(String id) {
//        List<String> transactionIfNeeded = getTransactionIfNeeded(id);
//    }

    public Iterable<Fix> getTransactionIfNeeded(SocketChannel socketChannel, String id) {

//        System.out.println("id: " + id);
//        Iterable<Fix> all1 = repository.findAll();
//        System.out.println("TRANSACTIONS ALL:");
//        all1.forEach(fix -> {
//            System.out.println(fix.getBrokerId() + " " + fix.isStatus());
//        });

        List<Fix> all = repository.findByBrokerIdAndStatusOrderByTime(id, false);
//        System.out.println("TRANSACTIONS:");
//        all.forEach(System.out::println);
//        List<String> transactions = new ArrayList<>();
//        for (Fix fix : all) {
//            transactions.add(fix.toString());
//        }
        return all;
    }

    public void updateFixMessageTransaction(Fix message) {
        repository.save(message);
    }
}

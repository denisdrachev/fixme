package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.model.Fix;
import ru.router.repository.TransactionRepository;

@Data
@Slf4j
public class Transactor implements Chain {

    private Chain next = null;
    private TransactionRepository repository;

    @Override
    synchronized public void handle(Fix message) {
        if (message.getSide() != null && ("3".equals(message.getDealType()) || "4".equals(message.getDealType()))) {
            message.setStatus(true);
        }

        if (message.getId() == null && message.getSide() != null
                || message.getId() != null && message.getSide() == null) {
            repository.save(message);
        }

        if (next != null && message.getSide() != null) {
            next.handle(message);
        }
    }
}

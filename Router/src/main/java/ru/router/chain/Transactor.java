package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.router.model.Fix;
import ru.router.repositories.TransactionRepository;

@Data
@Slf4j
public class Transactor implements Chain {

    private Chain next = null;

    private TransactionRepository repository;

    @Override
    synchronized public void handle(Fix message) {

        if ("3".equals(message.getDealType()) || "4".equals(message.getDealType())) {
            message.setStatus(true);
        }

        repository.save(message);
        System.err.println("message.getId(): " + message.getId());
        if (next != null) {
            next.handle(message);
        } else {
            Iterable<Fix> all = repository.findAll();
            for (Fix fix : all) {
                System.err.println(fix + " " + fix.getTime() + " " + fix.isStatus());
            }
        }
    }
}

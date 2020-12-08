package ru.router.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.router.model.Fix;
import ru.router.repositories.TransactionRepository;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Data
@Slf4j
@Component
public class Transactor implements Chain {

    private Chain next = null;

    @Autowired
    private TransactionRepository repository;

    @Override
    synchronized public void handle(Fix message) {
        repository.save(message);
        next.handle(message);
    }
}

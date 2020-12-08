package ru.router.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.router.model.Fix;

public interface TransactionRepository extends CrudRepository<Fix, Long> {
}

package ru.router.repository;

import org.springframework.data.repository.CrudRepository;
import ru.router.model.Fix;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Fix, Long> {
    List<Fix> findByBrokerIdAndStatusOrderByTime(String brokerId, boolean status);
}

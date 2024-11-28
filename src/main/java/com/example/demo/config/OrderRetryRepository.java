package com.example.demo.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRetryRepository extends JpaRepository<OrderRetryAttempt, Long> {
    List<OrderRetryAttempt> findByStatus(OrderRetryAttempt.RetryStatus status);
}
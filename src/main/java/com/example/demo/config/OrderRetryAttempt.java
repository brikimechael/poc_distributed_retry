package com.example.demo.config;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_retry_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRetryAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    private int attemptCount;

    @Lob
    private String serializedOrder;

    private String errorMessage;

    private LocalDateTime nextRetryTime;

    @Enumerated(EnumType.STRING)
    private RetryStatus status;

    public enum RetryStatus {
        PENDING, COMPLETED, FAILED
    }
}

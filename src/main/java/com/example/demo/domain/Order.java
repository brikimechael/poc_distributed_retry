package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements Serializable {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String customerId;

    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    @Transient
    private boolean simulateFailure = false;

    @Transient
    private int retryCount = 0;

    public enum OrderStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    public static Order create() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    public void simulateProcessingFailure() {
        this.simulateFailure = true;
        this.retryCount++;
    }
}


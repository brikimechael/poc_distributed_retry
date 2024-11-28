package com.example.demo.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    @NotNull(message = "Product ID is required")
    private String productId;

    @Column(name = "product_name")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    @Column(name = "quantity")
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    public BigDecimal getSubTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}


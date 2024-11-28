package com.example.demo.api;

import com.example.demo.domain.Order;
import com.example.demo.config.OrderRetryAttempt;
import com.example.demo.config.OrderRetryRepository;
import com.example.demo.service.OrderProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRetryController {
    private final RabbitTemplate rabbitTemplate;
    private final OrderRetryRepository retryRepository;
    private final OrderProcessingService processingService;
    private final ObjectMapper objectMapper;

    @PostMapping("/simulate-failure")
    public ResponseEntity<String> simulateOrderProcessingFailure() {
        // Create an order with simulated failure
        Order order = Order.create();
        order.simulateProcessingFailure();

        // Send to processing queue
        rabbitTemplate.convertAndSend("order-exchange", "order.process", order);

        return ResponseEntity.ok("Order submitted with simulated failure");
    }

    @GetMapping("/retry-attempts")
    public ResponseEntity<List<OrderRetryAttempt>> getRetryAttempts(
            @RequestParam(required = false) OrderRetryAttempt.RetryStatus status
    ) {
        List<OrderRetryAttempt> attempts = status != null
                ? retryRepository.findByStatus(status)
                : retryRepository.findAll();

        return ResponseEntity.ok(attempts);
    }

    @PostMapping("/manual-retry/{id}")
    public ResponseEntity<String> manualRetry(@PathVariable Long id) {
        OrderRetryAttempt attempt = retryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retry attempt not found"));

        try {
            Order order = objectMapper.readValue(attempt.getSerializedOrder(), Order.class);
            processingService.scheduleRetry(order, new Exception("Manual retry"));

            return ResponseEntity.ok("Manual retry scheduled");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to schedule manual retry: " + e.getMessage());
        }
    }
}


package com.example.demo.service;

import com.example.demo.config.OrderRetryAttempt;
import com.example.demo.config.OrderRetryRepository;
import com.example.demo.domain.Order;
import com.example.demo.job.OrderRetryJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class OrderProcessingService {
    // todo change these to dynamic values
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long[] BACKOFF_DELAYS = {
            1000,
            2000,
            4000,
            8000,
            16000
    };


    @Autowired
    private OrderRetryRepository retryRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "order-processing-queue")
    @Transactional
    public void processOrder(Order order) {
        try {
            if (order.isSimulateFailure()) {
                throw new RuntimeException("Simulated processing failure");
            }

            order.setStatus(Order.OrderStatus.COMPLETED);
            log.info("Order processed successfully: {}", order.getId());
        } catch (Exception e) {
            log.error("Order processing failed: {}", order.getId(), e);
            scheduleRetry(order, e);
        }
    }

    @Transactional
    public void scheduleRetry(Order order, Exception exception) {
        try {
            log.info("Scheduling retry for order: {}", order.getId());
            String serializedOrder = objectMapper.writeValueAsString(order);

            OrderRetryAttempt retryAttempt = OrderRetryAttempt.builder()
                    .orderId(order.getId())
                    .attemptCount(1)
                    .serializedOrder(serializedOrder)
                    .errorMessage(exception.getMessage())
                    .status(OrderRetryAttempt.RetryStatus.PENDING)
                    .nextRetryTime(LocalDateTime.now().plusSeconds(calculateNextRetryDelay(order.getRetryCount())))
                    .build();

            retryRepository.save(retryAttempt);

            scheduleRetryJob(retryAttempt);
        } catch (Exception e) {
            log.error("Failed to schedule retry", e);
        }
    }

    private void scheduleRetryJob(OrderRetryAttempt retryAttempt) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(OrderRetryJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .usingJobData("retryAttemptId", retryAttempt.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(retryAttempt.getNextRetryTime().atZone(ZoneId.systemDefault()).toInstant()))
                .build();
        log.info("Scheduling retry job for attempt: {}", retryAttempt.getId());
        scheduler.scheduleJob(job, trigger);
    }

    private long calculateNextRetryDelay(int attemptCount) {
        if (attemptCount > MAX_RETRY_ATTEMPTS) {
            return -1;
        }

        return attemptCount <= BACKOFF_DELAYS.length
                ? BACKOFF_DELAYS[attemptCount - 1]
                : BACKOFF_DELAYS[BACKOFF_DELAYS.length - 1];
    }
}

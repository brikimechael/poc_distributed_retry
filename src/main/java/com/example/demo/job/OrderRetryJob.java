package com.example.demo.job;

import com.example.demo.domain.Order;
import com.example.demo.config.OrderRetryAttempt;
import com.example.demo.config.OrderRetryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderRetryJob extends QuartzJobBean {
    @Autowired
    private OrderRetryRepository retryRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long retryAttemptId = context.getJobDetail()
                .getJobDataMap()
                .getLong("retryAttemptId");

        OrderRetryAttempt retryAttempt = retryRepository.findById(retryAttemptId)
                .orElseThrow(() -> new JobExecutionException("Retry attempt not found"));
        Order order = null;

        try {
             order = objectMapper.readValue(retryAttempt.getSerializedOrder(), Order.class);

            rabbitTemplate.convertAndSend("order-exchange", "order.process", order);

            retryAttempt.setStatus(OrderRetryAttempt.RetryStatus.COMPLETED);
            retryRepository.save(retryAttempt);
        } catch (Exception e) {
            retryAttempt.setAttemptCount(retryAttempt.getAttemptCount() + 1);

            if (retryAttempt.getAttemptCount() > 5) {
                rabbitTemplate.convertAndSend("order-exchange", "order.dead-letter", order);
                retryAttempt.setStatus(OrderRetryAttempt.RetryStatus.FAILED);
            }

            retryRepository.save(retryAttempt);
            log.error("Retry attempt failed", e);
        }
    }
}
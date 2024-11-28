package com.example.demo.exception;

import org.springframework.amqp.ImmediateAcknowledgeAmqpException;

public class ErrorNoRetryException extends ImmediateAcknowledgeAmqpException {  //NOSONAR amqp exception is required
    public ErrorNoRetryException(String message) {
        super(message);
    }
}

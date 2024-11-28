package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;

@Configuration
public class DistributedRetryConfiguration {

/*    @Primary
    @Bean
    @QuartzDataSource
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource defaultDatasource() {
        return DataSourceBuilder.create().build();
    }*/

    // RabbitMQ Queue Configurations
    @Bean
    public Queue originalQueue() {
        return new Queue("order-processing-queue", true);
    }

    @Bean
    public Queue retryQueue() {
        return new Queue("order-retry-queue", true);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("order-dead-letter-queue", true);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order-exchange");
    }

    @Bean
    public Binding originalQueueBinding(Queue originalQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(originalQueue)
                .to(orderExchange)
                .with("order.process");
    }

    @Bean
    public Binding retryQueueBinding(Queue retryQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(retryQueue)
                .to(orderExchange)
                .with("order.retry");
    }

    // RabbitMQ Message Converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(objectMapper());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter objectMapper() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.example.demo.domain");

        converter.setClassMapper(classMapper);
        return converter;
    }

    // Quartz Scheduler Configuration
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        return new SpringBeanJobFactory();
    }

    @Bean
    @Primary
    public SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource,
            SpringBeanJobFactory springBeanJobFactory
    ) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(springBeanJobFactory);
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10);

        return factory;
    }
}
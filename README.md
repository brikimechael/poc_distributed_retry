# Distributed Retry Mechanism - PoC Application

This is a **Proof of Concept (PoC)** Spring Boot application designed to demonstrate a **distributed retry mechanism** in an **event-driven architecture**. The implementation leverages **AMQP** for message communication and currently uses **RabbitMQ** as the message broker. However, the design is flexible and can be easily adapted to work with other brokers such as **ActiveMQ** or **Kafka** by modifying the configuration classes.

## Key Features
- **Distributed Retry Mechanism**: Ensures reliable event processing with robust retry logic for failed events.
- **Quartz Integration**: Synchronizes retries across multiple application instances and manages distributed locks for consistent retry handling.
- **Broker Agnostic Design**: Although RabbitMQ is the default, the configuration supports other AMQP-compliant brokers.

## Technology Stack
- **Java 21**: Modern language features for better performance and code clarity.
- **Spring Boot**: Simplifies application development and configuration.
- **RabbitMQ**: Default AMQP broker for reliable message queuing.
- **Quartz Scheduler**: Manages retry scheduling and synchronization across distributed instances.

## Usage Scenarios
This application is suitable for scenarios requiring:
- Reliable event-driven processing.
- Distributed systems with consistent retry logic across multiple nodes.
- Broker-agnostic messaging solutions.

---  

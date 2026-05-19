package com.ecommerce.inventoryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue inventoryQueue(){
        return new Queue("inventory-queue", true);
    }
    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange("order-events");
    }

    @Bean
    public Binding binding(Queue inventoryQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(inventoryQueue).to(orderEventExchange).with("order.placed");
    }
}

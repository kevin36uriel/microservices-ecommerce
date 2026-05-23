package com.ecommerce.orderservice.config;
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
    public static final String EXCHANGE_NAME = "order-events";

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue orderConfirmedQueue(){
        return new Queue("inventory-confirmed-queue", true);
    }

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding confirmedBinding(Queue orderConfirmedQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(orderEventExchange).with("order.confirmed");
    }

    @Bean
    public Queue orderCancelledQueue(){
        return new Queue("inventory-cancelled-queue", true);
    }

    @Bean
    public Binding cancelBinding(Queue orderCancelledQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderEventExchange).with("order.cancelled");
    }
}

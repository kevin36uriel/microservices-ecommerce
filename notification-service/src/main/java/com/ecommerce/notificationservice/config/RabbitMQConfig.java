package com.ecommerce.notificationservice.config;

import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import com.ecommerce.notificationservice.event.OrderConfirmedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Confiamos en los paquetes para evitar bloqueos de seguridad
        classMapper.setTrustedPackages("*");

        // MAPEAMOS LAS IDENTIDADES (Basado en tus logs reales)
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Tus logs dicen que el Inventario manda 'OrderPlacedEvent' cuando confirma
        idClassMapping.put("com.ecommerce.inventoryservice.event.OrderPlacedEvent", OrderConfirmedEvent.class);

        // Y manda 'OrderCancelledEvent' cuando falla
        idClassMapping.put("com.ecommerce.inventoryservice.event.OrderCancelledEvent", OrderCancelledEvent.class);

        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper); // Usamos el ClassMapper moderno

        return converter;
    }

    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable("notification-queue")
                .withArgument("x-dead-letter-exchange", "notification-dlx")
                .withArgument("x-dead-letter-routing-key", "notification.dead")
                .build();
    }
    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange("order-events");
    }

    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(notificationQueue).to(orderEventExchange).with("order.confirmed");
    }

    @Bean
    public Binding cancelledBinding(Queue notificationQueue, TopicExchange orderEventExchange) {
        return BindingBuilder.bind(notificationQueue).to(orderEventExchange).with("order.cancelled");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("notification-dlx");
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("notification-dlq", true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("notification.dead");
    }

}

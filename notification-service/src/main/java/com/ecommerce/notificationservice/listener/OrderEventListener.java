package com.ecommerce.notificationservice.listener;

import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import com.ecommerce.notificationservice.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = "notification-queue")
public class OrderEventListener {
    private final JavaMailSender mailSender;

    @RabbitHandler
    public void handlerOrderConfirmedPlacedEvent(OrderConfirmedEvent event) {
        log.info("Pedido confirmado para la Order: {}", event.orderNumber());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Orden confirmada " + event.orderNumber());
        message.setText("Hola!\n\n" +
                "Tu pedido con numero " + event.orderNumber() + " ha sido recibido exitosamente. \n" +
                "Pronto recibiras mas noticias sobre el envio. \n\n" +
                "Gracias por la compra");
        mailSender.send(message);
        log.info("Enviando correo de confirmacion a: {}", event.email());
        log.info("Correo enviado exitosamente: {}", event.email());

    }

    @RabbitHandler
    public void handlerOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Envio de correo de cancelacion para la orden: {}", event.orderNumber());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Actualizacion de tu pedido " + event.orderNumber());
        message.setText("Hola!\n\n" +
                "Tu pedido con numero " + event.orderNumber() + " ha sido cancelado. \n" +
                "Motivo: " + event.reason() + "\n\n" +
                "Si tienes alguna pregunta, no dudes en contactarnos. \n\n" +
                "Gracias por tu comprension");
        mailSender.send(message);
        log.info("Correo de cancelacion enviado exitosamente: {}", event.email());
    }
}

package com.ecommerce.notificationservice.listener;

import com.ecommerce.notificationservice.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final JavaMailSender mailSender;

    @RabbitListener(queues = "notification-queue")
    public void handlerOrderPlacedEvent(OrderPlacedEvent event){
        log.info("Notificacion enviada para la Order: {}", event.orderNumber());
            try{
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("kevin789uriel@gmail.com");
                message.setTo(event.email());
                message.setSubject("Orden confirmada " + event.orderNumber());
                message.setText("Hola!\n\n"+
                        "Tu pedido con numero " + event.orderNumber() + " ha sido recibido exitosamente. \n" +
                        "Pronto recibiras mas noticias sobre el envio. \n\n" +
                        "Gracias por la compra");
                mailSender.send(message);
                log.info("Enviando correo de confirmacion a: {}", event.email());
                log.info("Correo enviado exitosamente: {}", event.email());
            }catch (Exception e){
                log.error("Error al enviar el correo para la orden {}", event.email());
            }
    }
}

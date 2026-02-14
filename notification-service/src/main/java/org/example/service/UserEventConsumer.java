package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.MailRequest;
import org.example.dto.UserEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "user-event", groupId = "notification-group")
    public void handleUserEvent(UserEvent event) {
        String subject;
        String text;

        System.out.println("Пришло сообщение с Кафки!");

        if (event.getOperation().equals("CREATE")) {
            subject = "user-notification-api: Аккаунт создан!";
            text = "Здравствуйте! Ваш аккаунт был успешно создан!";
        } else if (event.getOperation().equals("DELETE")) {
            subject = "user-notification-api: Аккаунт удалён!";
            text = "Здравствуйте! Ваш аккаунт был успешно удалён!";
        } else {
            return;
        }

        emailService.sendMail(new MailRequest(event.getEmail(), subject, text));
    }
}

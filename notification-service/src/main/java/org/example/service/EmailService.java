package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.MailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(MailRequest mailRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailRequest.getTo());
        message.setSubject(mailRequest.getSubject());
        message.setText(mailRequest.getText());
        javaMailSender.send(message);
    }
}

package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {
        kafkaTemplate.send("user-event", event);
    }
}

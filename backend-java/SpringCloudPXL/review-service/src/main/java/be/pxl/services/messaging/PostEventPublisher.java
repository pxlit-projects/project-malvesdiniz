package be.pxl.services.messaging;

import be.pxl.services.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendApprovedEvent(UUID postId) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "post.approved",
                postId.toString()
        );
    }

    public void sendRejectedEvent(UUID postId, String reason) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "post.rejected",
                postId + "|" + reason
        );
    }
}

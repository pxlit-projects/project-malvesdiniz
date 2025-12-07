package be.pxl.services.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "post-exchange";
    public static final String APPROVED_QUEUE = "post-approved";
    public static final String REJECTED_QUEUE = "post-rejected";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue approvedQueue() {
        return new Queue(APPROVED_QUEUE);
    }

    @Bean
    public Queue rejectedQueue() {
        return new Queue(REJECTED_QUEUE);
    }

    @Bean
    public Binding approveBinding(Queue approvedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(approvedQueue).to(exchange).with("post.approved");
    }

    @Bean
    public Binding rejectBinding(Queue rejectedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(rejectedQueue).to(exchange).with("post.rejected");
    }
}

package com.byt3social.apigateway.amqp.publish;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogAMQPConfiguration {
    @Bean
    public FanoutExchange prospeccaoFanoutExchange() {
        return new FanoutExchange("api-gateway.ex");
    }
}

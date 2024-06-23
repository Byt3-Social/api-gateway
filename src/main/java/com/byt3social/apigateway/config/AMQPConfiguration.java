package com.byt3social.apigateway.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AMQPConfiguration {
    @Bean
    public RabbitAdmin criarRabbitAdmin(ConnectionFactory conexao) {
        return new RabbitAdmin(conexao);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializarAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

    @Bean
    public Jackson2JsonMessageConverter conversor() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory conexao, Jackson2JsonMessageConverter conversor) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(conexao);
        rabbitTemplate.setMessageConverter(conversor);

        return rabbitTemplate;
    }
}

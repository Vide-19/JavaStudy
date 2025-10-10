package com.javastudy.my_project_backend.config;

import com.alibaba.fastjson2.JSON;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class RabbitConfiguration {
    //邮件队列
    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable("mail")
                .build();
    }
    // ===== 使用 Fastjson2 的 MessageConverter =====
    @Bean
    public MessageConverter messageConverter() {
        return new MessageConverter() {
            @Override
            public org.springframework.amqp.core.Message toMessage(Object object, org.springframework.amqp.core.MessageProperties messageProperties) throws MessageConversionException {
                byte[] body = JSON.toJSONBytes(object);
                messageProperties.setContentType("application/json");
                messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
                return new org.springframework.amqp.core.Message(body, messageProperties);
            }

            @Override
            public Object fromMessage(org.springframework.amqp.core.Message message) throws MessageConversionException {
                byte[] body = message.getBody();
                if (body == null || body.length == 0) {
                    return null;
                }
                String json = new String(body, StandardCharsets.UTF_8);
                return JSON.parseObject(json, Object.class); // 返回 Map<String, Object>
            }
        };
    }

    // ===== 配置 RabbitTemplate 使用 Fastjson2 转换器 =====
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

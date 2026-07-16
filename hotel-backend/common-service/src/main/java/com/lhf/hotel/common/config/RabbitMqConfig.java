package com.lhf.hotel.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class RabbitMqConfig {

    static final String EXCHANGE = "hotel.order.events";

    static final String QUEUE_NOTIFY = "hotel.order.notify";
    static final String QUEUE_POINTS = "hotel.order.points";
    static final String QUEUE_CLEANING = "hotel.order.cleaning";
    static final String QUEUE_FINANCE = "hotel.order.finance";

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderNotifyQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFY).build();
    }

    @Bean
    public Queue orderPointsQueue() {
        return QueueBuilder.durable(QUEUE_POINTS).build();
    }

    @Bean
    public Queue orderCleaningQueue() {
        return QueueBuilder.durable(QUEUE_CLEANING).build();
    }

    @Bean
    public Queue orderFinanceQueue() {
        return QueueBuilder.durable(QUEUE_FINANCE).build();
    }

    @Bean
    public Binding bindNotify() {
        return BindingBuilder.bind(orderNotifyQueue()).to(orderEventExchange()).with("order.#");
    }

    @Bean
    public Binding bindPoints() {
        return BindingBuilder.bind(orderPointsQueue()).to(orderEventExchange()).with("order.checkout");
    }

    @Bean
    public Binding bindCleaningCheckout() {
        return BindingBuilder.bind(orderCleaningQueue()).to(orderEventExchange()).with("order.checkout");
    }

    @Bean
    public Binding bindCleaningRoomChanged() {
        return BindingBuilder.bind(orderCleaningQueue()).to(orderEventExchange()).with("order.roomChanged");
    }

    @Bean
    public Binding bindFinancePaid() {
        return BindingBuilder.bind(orderFinanceQueue()).to(orderEventExchange()).with("order.paid");
    }

    @Bean
    public Binding bindFinanceRefund() {
        return BindingBuilder.bind(orderFinanceQueue()).to(orderEventExchange()).with("order.refund");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

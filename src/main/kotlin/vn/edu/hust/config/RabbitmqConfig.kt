package vn.edu.hust.config

import com.chat.bot.service.interfaces.ai.MessageReceiver
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
class RabbitmqConfig (
    @Autowired val messageReceiver: MessageReceiver
) {

    @Bean
    fun chatbotQueue(): Queue {
        return QueueBuilder.durable("chatbot-queue")
            .withArgument("x-dead-letter-exchange", "chatbot-dlx")
            .withArgument("x-dead-letter-routing-key", "chatbot-dead")
            .build()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        return RabbitTemplate(connectionFactory)
    }

    @Bean
    fun container(connectionFactory: ConnectionFactory, listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory
        container.setQueueNames("chatbot-queue")
        container.setMessageListener(listenerAdapter)
        return container
    }

    @Bean
    fun listenerAdapter(): MessageListenerAdapter {
        return MessageListenerAdapter(messageReceiver, "receiveMessage")
    }

    @Bean
    fun deadLetterQueue(): Queue {
        return Queue("chatbot-dead-queue", true)
    }

    @Bean
    fun deadLetterExchange(): DirectExchange {
        return DirectExchange("chatbot-dlx")
    }

    @Bean
    fun deadLetterBinding(): Binding {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("chatbot-dead")
    }

    @Bean
    fun chatbotExchange(): DirectExchange {
        return DirectExchange("chatbot-exchange")
    }

    @Bean
    fun binding(): Binding {
        return BindingBuilder
            .bind(chatbotQueue())
            .to(chatbotExchange())
            .with("chatbot-queue")
    }

}
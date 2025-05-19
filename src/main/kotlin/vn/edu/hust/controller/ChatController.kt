package vn.edu.hust.controller

import vn.edu.hust.service.interfaces.ai.MessageReceiver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/chat")
class ChatController (
    @Autowired val rabbitTemplate: RabbitTemplate,
    @Autowired val messageReceiver: MessageReceiver
)  {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/ask", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun ask(@RequestBody query: Map<String, String>): Flux<ServerSentEvent<String>> {
        rabbitTemplate.convertAndSend("chatbot-queue", query["query"] ?: throw IllegalArgumentException("Query parameter is missing"))
        return messageReceiver.getEvents()
    }

}
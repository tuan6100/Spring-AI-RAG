package vn.edu.hust.service.implementations.ai

import vn.edu.hust.service.interfaces.ai.ChatBotService
import vn.edu.hust.service.interfaces.ai.MessageReceiver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers

@Service
class MessageReceiverImpl (
    @Autowired val chatBotService: ChatBotService
) : MessageReceiver {

    private val sink: Sinks.Many<ServerSentEvent<String>> = Sinks.many().multicast().onBackpressureBuffer()
    private val logger = KotlinLogging.logger {}

    @RabbitListener(queues = ["chatbot-queue"])
    override fun receiveMessage(query: String) {
        try {
            chatBotService.generateAnswer(query)
                .subscribeOn(Schedulers.boundedElastic())
                .map { token ->
                    ServerSentEvent.builder<String>()
                        .event("token")
                        .data(token)
                        .build()
                }
                .doOnNext { event -> sink.tryEmitNext(event) }
                .doOnComplete {
                    sink.tryEmitNext(
                        ServerSentEvent.builder<String>()
                            .event("end")
                            .data("DONE")
                            .build()
                    )
                    sink.tryEmitComplete()
                }
                .doOnError { throw it }
                .subscribe()
        } catch (e: Exception) {
            throw AmqpRejectAndDontRequeueException("Failed to process message", e)
        }
    }

    override fun getEvents(): Flux<ServerSentEvent<String>> {
        return sink.asFlux()
    }
}
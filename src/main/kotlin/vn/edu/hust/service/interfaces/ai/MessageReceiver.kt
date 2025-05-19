package vn.edu.hust.service.interfaces.ai

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
interface MessageReceiver {

    fun receiveMessage(message: String)
    fun getEvents(): Flux<ServerSentEvent<String>>
}
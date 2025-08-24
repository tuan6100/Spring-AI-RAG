package vn.edu.hust.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import vn.edu.hust.service.interfaces.ai.ChatBotService

@RestController
@RequestMapping("/api/v1/chat")
class ChatController (
    @field:Autowired val chatBotService: ChatBotService
) {
    @PostMapping("/ask", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun ask(@RequestBody query: Map<String, String>): Flux<ServerSentEvent<String>> {
        val queryString = query["query"] ?: throw IllegalArgumentException("Query parameter is missing")
        return chatBotService.generateAnswer(queryString)
            .map { content ->
                ServerSentEvent.builder<String>()
                    .data(content)
                    .build()
            }
    }
}


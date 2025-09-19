package vn.edu.hust.controller.require

import reactor.core.publisher.Flux

interface IChatBotService {

    suspend fun generateAnswer(query: String): Flux<String>

}
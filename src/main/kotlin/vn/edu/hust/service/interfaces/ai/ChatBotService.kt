package vn.edu.hust.service.interfaces.ai

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
interface ChatBotService {

    fun generateAnswer(query: String): Flux<String>

}
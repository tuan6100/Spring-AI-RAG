package vn.edu.hust.service.implementations.ai

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import vn.edu.hust.service.interfaces.ai.ChatBotService
import vn.edu.hust.service.interfaces.ai.RagService


@Service
class ChatBotServiceImpl(
    @Autowired private val chatClient: ChatClient,
    @Autowired private val ragService: RagService,
    @Autowired private val vectorStore: VectorStore,
    @Autowired private val chatMemory: ChatMemory
) : ChatBotService {

    private val logger = KotlinLogging.logger {}

    override suspend fun generateAnswer(query: String): Flux<String> {
        if (query.isBlank()) {
            return Flux.error(IllegalArgumentException("Query cannot be null or empty."))
        }
        logger.info { "Query: $query" }
        try {
            val documents = ragService.search(query)
            if (documents.isNullOrEmpty()) {
                return Flux.just("No relevant information found for your query.")
            }
            val context: String = documents.joinToString("\n\n") { it.text.toString() }.trim()
            if (context.isBlank()) {
                return Flux.just("No context available to generate an answer.")
            }
            logger.info { "Context: $context \n" }
            val promptTemplate: PromptTemplate? = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(
                    """
            <query>

            Context information is below.

			---------------------
			<question_answer_context>
			---------------------

			Given the context information and no prior knowledge, answer the query.

			Follow these rules:

			1. If the answer is not in the context, just say that you don't know.
			2. Avoid statements like "Based on the context..." or "The provided information...".
            
            """.trimIndent()
                )
                .build()
            var answer = ""
            return chatClient.prompt(promptTemplate.create())
                .advisors {
                    MessageChatMemoryAdvisor(chatMemory)
                    QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().similarityThreshold(0.6).topK(10).build())
                    SimpleLoggerAdvisor()
                }
                .user(query)
                .stream()
                .content()
                .map { it.toString() }
                .doOnNext { answerPart -> answer += answerPart }
                .doOnComplete { logger.info { "Answer: $answer \n" } }
        } catch (e: Exception) {
            logger.error { "Error occurred: ${e.message}" }
            return Flux.error(RuntimeException("Sorry, the service is currently unavailable. Please try again later."))
        }
    }

}
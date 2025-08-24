package vn.edu.hust.config


import com.knuddels.jtokkit.api.EncodingType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Collections.CollectionInfo
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemory
import org.springframework.ai.embedding.BatchingStrategy
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.TokenCountBatchingStrategy
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.DefaultResourceLoader


@Configuration
class AiConfig (
    @Autowired  val embeddingModel: EmbeddingModel
){

    private val logger = KotlinLogging.logger {}

    @Bean
    fun chatMemory(): ChatMemory {
        return InMemoryChatMemory()
    }

    @Bean
    fun chatClient(builder: ChatClient.Builder, chatMemory: ChatMemory): ChatClient {
        val resource = DefaultResourceLoader().getResource("classpath:base.yaml")
        val content = resource.inputStream.bufferedReader().use { it.readText() }
        return builder.defaultSystem(content)
            .defaultAdvisors { MessageChatMemoryAdvisor(chatMemory) }
            .build()
    }

    @Bean
    fun qdrantClient(): QdrantClient {
        val client = QdrantClient(
            QdrantGrpcClient.newBuilder("localhost", 6334, false).build()
        )
        val collectionInfo: CollectionInfo? = client.getCollectionInfoAsync("ai-chat-bot").get()
        logger.info { "Collection info: $collectionInfo" }
        return client
    }

    @Bean
    fun vectorStore(qdrantClient: QdrantClient): VectorStore {
        logger.info { "Using Qdrant vector store: ${qdrantClient::class.java.name}" }
        logger.info { "Using embedding model: $embeddingModel" }
        return QdrantVectorStore.builder(qdrantClient, embeddingModel)
            .collectionName("ai-chat-bot")
            .batchingStrategy(TokenCountBatchingStrategy())
            .build()
    }

    @Bean
    fun batchingStrategy(): BatchingStrategy {
        return TokenCountBatchingStrategy(
            EncodingType.O200K_BASE,
            3072,
            0.1
        )
    }


}
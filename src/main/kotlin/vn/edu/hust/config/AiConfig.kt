package vn.edu.hust.config


import com.knuddels.jtokkit.api.EncodingType
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.embedding.BatchingStrategy
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.TokenCountBatchingStrategy
import org.springframework.ai.transformers.TransformersEmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.DefaultResourceLoader
import vn.edu.hust.custom.CustomQdrantVectorStore
import java.io.File


@Configuration
class AiConfig {

    @Bean
    fun chatClient(
        chatMemory: ChatMemory,
        chatModel: ChatModel
    ): ChatClient {
        val resource = DefaultResourceLoader().getResource("classpath:prompt.yaml")
        val content = resource.inputStream.bufferedReader().use { it.readText() }
        val builder = ChatClient.builder(chatModel)
        return builder.defaultSystem(content)
            .defaultAdvisors { MessageChatMemoryAdvisor.builder(chatMemory).build() }
            .build()
    }

    @Bean
    fun embeddingModel(): EmbeddingModel {
        val dataFile = File("./model.onnx_data")
        if (!dataFile.exists()) {
            val resource = DefaultResourceLoader().getResource("classpath:transformer/model.onnx_data")
            if (resource.exists()) {
                resource.inputStream.use { input ->
                    dataFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        val embeddingModel = TransformersEmbeddingModel()
        embeddingModel.setModelResource("classpath:transformer/model.onnx")
        embeddingModel.setTokenizerResource("classpath:transformer/tokenizer.json")
        embeddingModel.setTokenizerOptions(mapOf(
            "addSpecialTokens" to "true",
            "truncation" to "true",
            "padding" to "true",
            "stride" to "0"
        ))
        embeddingModel.setModelOutputName("token_embeddings")
        embeddingModel.setDisableCaching(true)
        embeddingModel.afterPropertiesSet()
        return embeddingModel
    }

    @Bean
    fun qdrantClient(
        @Value("\${spring.ai.vectorstore.qdrant.host}") host: String,
        @Value("\${spring.ai.vectorstore.qdrant.port}") port: Int,
    ): QdrantClient = QdrantClient(
        QdrantGrpcClient.newBuilder(host, port, false).build()
    )

    @Bean
    @Primary
    fun vectorStore(
        qdrantClient: QdrantClient,
        embeddingModel: EmbeddingModel,
        @Value("\${spring.ai.vectorstore.qdrant.collection-name}") collectionName: String,
        @Value("\${spring.ai.vectorstore.qdrant.vector-name}") vectorName: String,
    ): VectorStore {
        val custom = CustomQdrantVectorStore.builder {
            this.qdrantClient = qdrantClient
            this.embeddingModel = embeddingModel
            this.collectionName = collectionName
            this.vectorName = vectorName
            initializeSchema = true
        }
        custom.afterPropertiesSet()
        return custom
    }


    @Bean
    fun batchingStrategy(): BatchingStrategy = TokenCountBatchingStrategy(
            EncodingType.O200K_BASE,
            1024,
            0.1
        )


}
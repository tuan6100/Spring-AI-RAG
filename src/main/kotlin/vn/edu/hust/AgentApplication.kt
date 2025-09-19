package vn.edu.hust


import io.github.oshai.kotlinlogging.KotlinLogging
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections.CollectionInfo
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File


@SpringBootApplication
class BotApplication(
    @field:Autowired final val vectorStore: VectorStore,
    @field:Autowired final val qdrantClient: QdrantClient,
    @field:Autowired final val embeddingModel: EmbeddingModel,
) {
    private val logger = KotlinLogging.logger {}

    @Value("\${spring.ai.vectorstore.qdrant.collection-name}")
    private lateinit var collectionName: String

    @PostConstruct
    fun getQdrantInfo() {
        logger.info { "Using Qdrant vector store: ${vectorStore::class.java.name}" }
        logger.info { "Using embedding model: $embeddingModel with vector dimensions: ${embeddingModel.dimensions()}" }
        val collectionInfo: CollectionInfo? = qdrantClient.getCollectionInfoAsync(collectionName).get()
        logger.info { "Collection info: ${collectionInfo ?: "Not found"}" }
        logger.info {  }
    }

    @PreDestroy
    fun removeTempFile() {
        val file = File("./model.onnx_data")
        if (file.exists()) {
            file.delete()
            logger.info { "Deleted file ${file.name}" }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<BotApplication>(*args)
}

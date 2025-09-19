package vn.edu.hust.custom

import io.github.oshai.kotlinlogging.KotlinLogging
import io.qdrant.client.QdrantClient
import io.qdrant.client.WithPayloadSelectorFactory
import io.qdrant.client.grpc.Points
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.model.EmbeddingUtils
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore
import org.springframework.beans.factory.InitializingBean

class CustomQdrantVectorStore private constructor(
    builder: QdrantVectorStore.Builder,
    private val qdrantClient: QdrantClient,
    private val collectionName: String,
    private val vectorName: String
): AbstractObservationVectorStore(builder), InitializingBean {

    private val logger = KotlinLogging.logger {}

    private val qdrantVectorStore: QdrantVectorStore = builder.build()

    override fun doAdd(documents: List<Document?>) {
        return qdrantVectorStore.doAdd(documents)
    }

    override fun doDelete(idList: List<String?>) {
        return qdrantVectorStore.doDelete(idList)
    }

    override fun doSimilaritySearch(request: SearchRequest): List<Document?> {
        val filter = Points.Filter.getDefaultInstance()
        val queryEmbedding = embeddingModel.embed(request.query)
        logger.debug{ "Embedded: ${queryEmbedding.joinToString { "$it" }}" }
        val searchPoints = Points.SearchPoints.newBuilder()
            .setCollectionName(collectionName)
            .setVectorName(vectorName)
            .setLimit(request.topK.toLong())
            .setWithPayload(WithPayloadSelectorFactory.enable(true))
            .addAllVector(EmbeddingUtils.toList(queryEmbedding))
            .setFilter(filter)
            .setScoreThreshold(request.similarityThreshold.toFloat())
            .build()
        val queryResponse: MutableList<Points.ScoredPoint> = qdrantClient.searchAsync(searchPoints).get()
        logger.debug { "Search result: $queryResponse" }
        return if (queryResponse.isEmpty()) listOf() else
            queryResponse.map { scoredPoint ->
                Document.builder()
                    .id(scoredPoint.id.toString())
                    .text(scoredPoint.payloadMap.values.joinToString(" ") { value ->
                        when {
                            value.hasStringValue() -> value.stringValue
                            value.hasIntegerValue() -> value.integerValue.toString()
                            value.hasDoubleValue() -> value.doubleValue.toString()
                            value.hasBoolValue() -> value.boolValue.toString()
                            value.hasListValue() -> value.listValue.valuesList.joinToString(", ") { it.toString() }
                            value.hasStructValue() -> value.structValue.fieldsMap.values.joinToString(", ") { structValue ->
                                when {
                                    structValue.hasStringValue() -> structValue.stringValue
                                    structValue.hasIntegerValue() -> structValue.integerValue.toString()
                                    structValue.hasDoubleValue() -> structValue.doubleValue.toString()
                                    structValue.hasBoolValue() -> structValue.boolValue.toString()
                                    else -> structValue.toString()
                                }
                            }
                            else -> value.toString()
                        }
                    })
                    .score(scoredPoint.score.toDouble())
                    .build()
            }
    }

    override fun createObservationContextBuilder(operationName: String): VectorStoreObservationContext.Builder {
        return qdrantVectorStore.createObservationContextBuilder(operationName)
    }

    override fun afterPropertiesSet() {
        qdrantVectorStore.afterPropertiesSet()
    }

    class Builder {
        lateinit var qdrantClient: QdrantClient
        lateinit var embeddingModel: EmbeddingModel
        var collectionName: String = "vector_store"
        var vectorName: String = "default"
        var initializeSchema: Boolean = false

        internal fun build(): CustomQdrantVectorStore {
            val builder = QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(initializeSchema)
            return CustomQdrantVectorStore(builder, qdrantClient, collectionName, vectorName)
        }
    }

    companion object {
        fun builder(build: Builder.() -> Unit): CustomQdrantVectorStore {
            return Builder().apply(build).build()
        }
    }
}
package vn.edu.hust

import org.springframework.ai.autoconfigure.vectorstore.cassandra.CassandraVectorStoreAutoConfiguration
import org.springframework.ai.autoconfigure.vectorstore.qdrant.QdrantVectorStoreAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [QdrantVectorStoreAutoConfiguration::class, CassandraVectorStoreAutoConfiguration::class])
class BotApplication

fun main(args: Array<String>) {
    runApplication<BotApplication>(*args)
}

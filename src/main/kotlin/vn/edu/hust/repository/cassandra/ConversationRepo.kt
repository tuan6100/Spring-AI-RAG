package vn.edu.hust.repository.cassandra

import vn.edu.hust.data.cassandra.client.Conversation
import org.springframework.data.cassandra.repository.CassandraRepository
import java.util.UUID

interface ConversationRepo : CassandraRepository<Conversation, UUID> {
}
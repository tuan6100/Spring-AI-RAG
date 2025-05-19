package vn.edu.hust.repository.cassandra

import vn.edu.hust.data.cassandra.client.Message
import org.springframework.data.cassandra.repository.CassandraRepository
import java.util.UUID

interface MessageRepo : CassandraRepository<Message, UUID> {
}
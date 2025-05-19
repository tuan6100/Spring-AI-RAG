package vn.edu.hust.data.cassandra.client

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.Date
import java.util.UUID

@Table
data class Conversation(
    @PrimaryKey val conversationId: UUID,
    @Column val title: String,
    @Column val accountId: UUID,
    @Column val createdAt: Date,
)

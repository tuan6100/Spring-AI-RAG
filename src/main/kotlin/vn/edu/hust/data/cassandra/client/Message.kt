package vn.edu.hust.data.cassandra.client

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.Date
import java.util.UUID

@Table
data class Message(
    @PrimaryKey val messageId: UUID,
    @Column val conversationId: UUID,
    @Column val isHuman: Boolean,
    @Column val content: String,
    @Column val sentAt: Date,
)

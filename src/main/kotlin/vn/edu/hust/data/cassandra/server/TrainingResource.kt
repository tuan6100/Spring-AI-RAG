package vn.edu.hust.data.cassandra.server

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.UUID
import java.util.Date

@Table
data class TrainingResource(
    @PrimaryKey val resourceId: UUID,
    @Column val title: String,
    @Column val uri: String,
    @Column val trainingType: String,
    @Column val trainedAt: Date
)

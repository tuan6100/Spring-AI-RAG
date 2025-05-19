package vn.edu.hust.data.cassandra.client

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.UUID


@Table
data class Account(
    @PrimaryKey val accountId: UUID,
    @Column val username: String,
    @Column val email: String,
    @Column val password: String,
)

package vn.edu.hust.repository.cassandra

import vn.edu.hust.data.cassandra.client.Account
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AccountRepo : CassandraRepository<Account, UUID> {

}
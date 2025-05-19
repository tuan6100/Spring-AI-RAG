package vn.edu.hust.service.interfaces.user

import org.springframework.stereotype.Service
import java.util.UUID

@Service
interface TokenProviderService {

    val JWT_SECRET: String

    val ACCESS_TOKEN_EXPIRATION: Long

    val REFRESH_TOKEN_EXPIRATION: Long

    fun generateAccessToken(id: UUID): String

    fun generateRefreshToken(id: UUID): String

    fun getCurrentUserId(): UUID
}
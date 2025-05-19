package vn.edu.hust.service.interfaces.user

import org.springframework.stereotype.Service

@Service
interface EmailEncryptionService {

    fun encryptEmail(email: String): String

    fun decryptEmail(email: String): String
}
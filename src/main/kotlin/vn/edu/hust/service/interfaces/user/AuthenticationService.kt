package vn.edu.hust.service.interfaces.user

import org.springframework.stereotype.Service

@Service
interface AuthenticationService {

    fun login(email: String, password: String): String

    fun register(email: String, password: String): String
}
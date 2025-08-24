package vn.edu.hust.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.HttpStatus

@RestControllerAdvice
class GlobalException {

    @ExceptionHandler(AiException::class)
    fun handleAiException(e: AiException): ResponseEntity<Any> {
        val responseBody = mapOf("message" to e.message)
        return ResponseEntity(responseBody, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherException(e: Exception): ResponseEntity<Any> {
        val responseBody = mapOf("message" to e.message)
        return ResponseEntity(responseBody, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
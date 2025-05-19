package vn.edu.hust.service.interfaces.ai

import org.springframework.ai.document.Document
import org.springframework.stereotype.Service

@Service
interface RagService {

    fun search(query: String): List<Document>?

    fun loadDataFromWeb(url: String)

    fun loadDataFromFile(path: String)

    fun delete(document: String)

    fun update(document: String, vector: List<Float>)

}
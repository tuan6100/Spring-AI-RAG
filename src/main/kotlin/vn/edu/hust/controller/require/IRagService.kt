package vn.edu.hust.controller.require

import org.springframework.ai.document.Document

interface IRagService {

    fun search(query: String): List<Document>?

    fun loadDataFromWeb(url: String)

    fun loadDataFromFile(path: String)

    fun delete(document: String)

    fun update(document: String, vector: List<Float>)

}
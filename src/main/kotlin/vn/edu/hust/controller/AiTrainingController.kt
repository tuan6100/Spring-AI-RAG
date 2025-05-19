package vn.edu.hust.controller

import vn.edu.hust.service.interfaces.ai.RagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/train")
class AiTrainingController (
    @Autowired val ragService: RagService
) {

    @PostMapping()
    fun train(@RequestParam type: String, @RequestBody source: Map<String, String>) {
        return when (type) {
            "web" -> ragService.loadDataFromWeb(source["url"]!!)
            "file" -> ragService.loadDataFromFile(source["path"]!!)
            else -> throw IllegalArgumentException("Invalid training type")
        }
    }
}
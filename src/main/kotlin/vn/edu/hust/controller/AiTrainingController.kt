package vn.edu.hust.controller

import vn.edu.hust.controller.require.IRagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/train")
class AiTrainingController (
    @field:Autowired final val iRagService: IRagService
) {

    @PostMapping()
    fun train(@RequestParam type: String, @RequestBody source: Map<String, String>) {
        return when (type) {
            "web" -> iRagService.loadDataFromWeb(source["url"]!!)
            "file" -> iRagService.loadDataFromFile(source["path"]!!)
            else -> throw IllegalArgumentException("Invalid training type")
        }
    }
}
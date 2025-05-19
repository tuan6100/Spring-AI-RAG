package vn.edu.hust.service.implementations.ai

import vn.edu.hust.exception.AiException
import vn.edu.hust.service.interfaces.ai.RagService
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.embedding.BatchingStrategy
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.JsonReader
import org.springframework.ai.reader.TextReader
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File


@Service
class RagServiceImpl (
    @Autowired val vectorStore: VectorStore,
    @Autowired val batchingStrategy: BatchingStrategy,
) : RagService {

    private val logger = KotlinLogging.logger {}

//    private val documentMap: MutableMap<String, Document> = mutableMapOf()

    override fun search(query: String): List<Document>? {
        return vectorStore.similaritySearch(query)
    }

    override fun loadDataFromWeb(url: String) {
        val jsoupDocument = Jsoup.connect(url).userAgent("Mozilla").get()
        val title = jsoupDocument.title()
        if (jsoupDocument == null) {
            throw AiException("Cannot get content from $url")
        }
        val content = StringBuilder()
        content.append(jsoupDocument.body().text()).append("Nguồn tham khảo: $url")
        logger.info {"Content length: ${content.length}"}
        val document = Document(content.toString())
        val tokens = TokenTextSplitter().split(document)
        val vectors = batchingStrategy.batch(tokens)
        for (vector in vectors) {
            logger.info { "${vectors.indexOf(vector) + 1}. Vector content: $vector" }
            vectorStore.add(vector)
        }
    }

    override fun loadDataFromFile(path: String) {
        val file = File(path)
        if (!file.exists()) {
            throw AiException("File $path not found")
        }
        val documentResource = FileSystemResource(file)
        var documentReader: DocumentReader? = null
        if (documentResource.filename.endsWith(".pdf")) {
            logger.info { "Loading PDF document" }
            documentReader = PagePdfDocumentReader(
                documentResource,
                PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(
                        ExtractedTextFormatter.builder()
                            .build()
                    )
                    .build()
            )
        } else if (documentResource.filename.endsWith(".docx") || documentResource.filename.endsWith(".pptx")) {
            documentReader = TikaDocumentReader(documentResource)
        } else if (documentResource.filename.endsWith(".csv")) {
            val jsonContent = convertCsvToJson(documentResource)
            documentReader = JsonReader(jsonContent)
        } else if (documentResource.filename.endsWith(".txt")) {
            documentReader = TextReader(documentResource)
        }
        if (documentReader != null) {
            val textSplitter = TokenTextSplitter()
            val rawDocuments: List<Document> = textSplitter.apply(documentReader.get())
            val cleanedDocuments: List<Document> = rawDocuments.map { doc ->
                Document(doc.text?.replace(Regex("\\s+"), " ")?.trim().toString())
            }
            logger.info { "Documents to be indexed: ${cleanedDocuments.size}" }
            cleanedDocuments.forEachIndexed { i, doc ->
                logger.info { "The content of the document $i: ${doc.text}..." }
            }
            logger.info { "Loading text document to Qdrant vector database" }
            vectorStore.accept(cleanedDocuments)
            logger.info { "Loaded text document to Qdrant vector database" }
        }
    }

    private fun convertCsvToJson(resource: Resource): Resource {
        val csvMapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader()
        val it: MappingIterator<Map<String, String>> = csvMapper.readerFor(Map::class.java)
            .with(schema)
            .readValues(resource.inputStream)
        val list = it.readAll()
        val jsonMapper = jacksonObjectMapper()
        val jsonString = jsonMapper.writeValueAsString(list)
        return ByteArrayResource(jsonString.toByteArray())
    }

//    @PostConstruct
//    fun trainWithTestDocument() {
//        val path = "D:\\WORK\\SOICT\\IT3160\\aiml-g1-report-nhap-mon-hoc-may-va-khai-pha-du-lieu-it3190.pdf"
//        try {
//            loadDataFromFile(path)
//        } catch (e: AiException) {
//            logger.error { e.message }
//        } catch (e: IOException) {
//            logger.error { e.message }
//        }
//    }
//
//    @PreDestroy
//    fun clearTestDocument() {
//        val path = "D:\\WORK\\SOICT\\IT3160\\aiml-g1-report-nhap-mon-hoc-may-va-khai-pha-du-lieu-it3190.pdf"
//        val document = documentMap[path]
//        try {
//            vectorStore.delete(document?.id.toString())
//        } catch (e: Exception) {
//            logger.error { "Failed to delete document from vector store: ${e.message}" }
//        }
//        documentMap.clear()
//    }

    override fun delete(document: String) {
        TODO("Not yet implemented")
    }

    override fun update(document: String, vector: List<Float>) {
        TODO("Not yet implemented")
    }

}
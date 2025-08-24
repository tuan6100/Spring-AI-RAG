package vn.edu.hust.config

//import reactor.netty.http.client.HttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Configuration
class WebClientConfig {

//    @Bean
//    fun webClient(): WebClient {
//        val httpClient = HttpClient.create()
//            .responseTimeout(Duration.ofSeconds(60))
//        return WebClient.builder()
//            .clientConnector(ReactorClientHttpConnector(httpClient))
//            .build()
//    }

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = HttpComponentsClientHttpRequestFactory()
        factory.setConnectTimeout(60000)
        factory.setReadTimeout(60000)
        return RestTemplate(factory)
    }

    @Bean
    fun restClientBuilder(): RestClient.Builder {
        return RestClient.builder()
    }
}
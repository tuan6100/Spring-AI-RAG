package vn.edu.hust.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
//import reactor.netty.http.client.HttpClient
import java.time.Duration

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
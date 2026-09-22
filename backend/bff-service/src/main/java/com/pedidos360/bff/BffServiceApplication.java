package com.pedidos360.bff;

import com.pedidos360.bff.config.AzureAdProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(AzureAdProperties.class)
public class BffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

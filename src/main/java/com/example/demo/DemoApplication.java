package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    // 👇 ADICIONE ESTE BLOCO AQUI (É isso que o Spring está procurando e não acha!)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}

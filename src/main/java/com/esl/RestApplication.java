package com.esl;

import com.esl.service.rest.ImageGenerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class RestApplication {
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public ExecutorService executionPool() {
		return Executors.newFixedThreadPool(20);
	}

	@Bean
	public TaskExecutor taskExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(25);
		return executor;
	}

	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		var filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(1000);
		filter.setIncludeHeaders(true);
		return filter;
	}

	@Value("${IMAGE_GENERATION_SERVER_HOST}") String imageGenerationServiceHost;
	@Value("${IMAGE_GENERATION_SERVER_APIKEY}") String imageGenerationServiceApiKey;

	@Bean
	public ImageGenerationService imageGenerationService() {
		return new ImageGenerationService(imageGenerationServiceHost, imageGenerationServiceApiKey, Duration.ofSeconds(30));
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}
}

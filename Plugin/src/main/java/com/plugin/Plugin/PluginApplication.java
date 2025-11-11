package com.plugin.Plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PluginApplication {

	public static void main(String[] args) {
		//SpringApplication.run(PluginApplication.class, args);

		SpringApplicationBuilder builder = new SpringApplicationBuilder(PluginApplication.class);

		builder.headless(false);

		ConfigurableApplicationContext context = builder.run(args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/plugin/getCertificates").allowedOrigins("http://localhost:3000").allowedHeaders("*");
				registry.addMapping("/plugin/sign").allowedOrigins("http://localhost:3000").allowedHeaders("*");

			}
		};
	}

}

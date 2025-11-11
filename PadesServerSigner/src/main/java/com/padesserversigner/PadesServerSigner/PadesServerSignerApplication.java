package com.padesserversigner.PadesServerSigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


import javax.sql.DataSource;

@SpringBootApplication
@EnableWebSecurity
public class PadesServerSignerApplication {

	public static void main(String[] args) {
		//correr aplicação PADES
		SpringApplication.run(PadesServerSignerApplication.class, args);

		//Utilizar para correr CCREADER - Plugin
		/*
		SpringApplicationBuilder builder = new SpringApplicationBuilder(PadesServerSignerApplication.class);

		builder.headless(false);

		ConfigurableApplicationContext context = builder.run(args);
		*/
	}
	@Bean
	public DataSource dataSource(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://mysql-container:3306/padesserversigner?useSSL=false&serverTimezone=UTC");
		dataSource.setUsername("username");
		dataSource.setPassword("password");
		return dataSource;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/cmd/dssFiler2SendOTP").allowedOrigins("http://localhost:3000").allowedHeaders("*");
				registry.addMapping("/cmd/dssFiler2Sign").allowedOrigins("http://localhost:3000");
			}
		};
	}
}

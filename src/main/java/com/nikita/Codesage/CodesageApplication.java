package com.nikita.Codesage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CodesageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodesageApplication.class, args);
	}

}

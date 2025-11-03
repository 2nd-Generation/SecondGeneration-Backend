package com.web.coreclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoreclassApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreclassApplication.class, args);
	}

}

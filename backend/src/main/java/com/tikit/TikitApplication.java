package com.tikit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TikitApplication {

	public static void main(String[] args) {
		SpringApplication.run(TikitApplication.class, args);
	}

}

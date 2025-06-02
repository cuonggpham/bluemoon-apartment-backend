package com.dev.tagashira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TagashiraApplication {

	public static void main(String[] args) {
		SpringApplication.run(TagashiraApplication.class, args);
	}

}

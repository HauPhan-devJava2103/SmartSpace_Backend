package com.vn.smart_space;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartSpaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartSpaceApplication.class, args);
	}

}

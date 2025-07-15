package com.myproject.prescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PrescriptionManageServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrescriptionManageServerApplication.class, args);
	}

}

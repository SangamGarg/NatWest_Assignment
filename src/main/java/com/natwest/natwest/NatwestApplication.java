package com.natwest.natwest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NatwestApplication {

	public static void main(String[] args) {
		SpringApplication.run(NatwestApplication.class, args);
		System.out.println("Server Running....");
	}

}

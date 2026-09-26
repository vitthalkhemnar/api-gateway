package com.ecom.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { ReactiveUserDetailsServiceAutoConfiguration.class })
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}

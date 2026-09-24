package com.ecom.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("user-service-route", r -> r.path("/user-service/**")
						.filters(f -> f.stripPrefix(1)
								.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_FIRST"))
						.uri("lb://USER-SERVICE"))
				.route("product-service-route", r -> r.path("/product-service/**")
						.filters(f -> f.stripPrefix(1)
								.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_FIRST"))
						.uri("lb://PRODUCT-SERVICE"))
				.route("order-service-route", r -> r.path("/order-service/**")
						.filters(f -> f.stripPrefix(1)
								.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_FIRST"))
						.uri("lb://ORDER-SERVICE"))
				.route("email-service-route", r -> r.path("/email-service/**")
						.filters(f -> f.stripPrefix(1)
								.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_FIRST"))
						.uri("lb://EMAIL-SERVICE"))
				.route("payment-service-route", r -> r.path("/payment-service/**")
						.filters(f -> f.stripPrefix(1)
								.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_FIRST"))
						.uri("lb://PAYMENT-SERVICE"))
				.build();
	}
}

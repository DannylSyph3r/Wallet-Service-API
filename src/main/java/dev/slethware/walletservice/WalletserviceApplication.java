package dev.slethware.walletservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
		info = @Info(
				contact = @Contact(
						name = "Slethware",
						email = "info@slethware.dev"
				),
				description = "Wallet Service API with Paystack Integration",
				title = "Wallet Service API Documentation",
				version = "1.0"
		),
		security = {
				@SecurityRequirement(name = "bearerAuth"),
				@SecurityRequirement(name = "apiKey")
		}
)
@SecurityScheme(
		name = "bearerAuth",
		description = "JWT authentication",
		scheme = "bearer",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
		name = "apiKey",
		description = "API Key authentication for service-to-service access",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.HEADER,
		paramName = "x-api-key"
)
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@SpringBootApplication
public class WalletserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletserviceApplication.class, args);
	}

}
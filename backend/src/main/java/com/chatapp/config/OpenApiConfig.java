package com.chatapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Chat Application API")
                        .version("1.0.0")
                        .description("REST API documentation for the Chat Application. " +
                                "This API provides endpoints for user authentication, " +
                                "real-time messaging, and chat room management. " +
                                "\n\n**Key Features:**\n" +
                                "- User registration and authentication with JWT\n" +
                                "- Real-time messaging via WebSocket (STOMP)\n" +
                                "- Direct (1-on-1) chat functionality\n" +
                                "- Message history with pagination\n" +
                                "- Typing indicators\n" +
                                "- Read receipts\n" +
                                "\n**Authentication:**\n" +
                                "Most endpoints require JWT authentication. " +
                                "Use the `/api/auth/login` endpoint to obtain a token, " +
                                "then include it in the Authorization header as: `Bearer <token>`")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@chatapp.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.chatapp.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login endpoint")));
    }
}
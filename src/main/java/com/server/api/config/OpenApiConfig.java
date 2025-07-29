package com.server.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 * Proporciona una interfaz gráfica para probar los endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.name:SGCCA API}")
    private String applicationName;

    @Value("${app.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;

    @Value("${app.description:Sistema de Gestión y Control de Accesos}")
    private String applicationDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers());
    }

    private Info buildInfo() {
        return new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description(applicationDescription + 
                    "\n\n### Características:\n" +
                    "- ✅ CRUD completo para todas las entidades\n" +
                    "- ✅ Autenticación y autorización con JWT\n" +
                    "- ✅ Auditoría de accesos y actividades\n" +
                    "- ✅ Gestión de sesiones de usuario\n" +
                    "- ✅ Control de permisos por tipo de usuario\n" +
                    "- ✅ Validaciones de negocio y seguridad\n\n" +
                    "### Tecnologías:\n" +
                    "- Spring Boot 3.5.4 con Java 21\n" +
                    "- PostgreSQL con JPA/Hibernate\n" +
                    "- Flyway para migraciones\n" +
                    "- Docker y Docker Compose\n" +
                    "- Principios SOLID y Clean Code")
                .contact(buildContact())
                .license(buildLicense());
    }

    private Contact buildContact() {
        return new Contact()
                .name("Equipo de Desarrollo SGCCA")
                .email("pablojuanfd@gmail.com")
                .url("https://github.com/JuanPabloFloresDiaz/CCA_API");
    }

    private License buildLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> buildServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor de Desarrollo Local");

        Server dockerServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor Docker Local");

        return List.of(localServer, dockerServer);
    }
}

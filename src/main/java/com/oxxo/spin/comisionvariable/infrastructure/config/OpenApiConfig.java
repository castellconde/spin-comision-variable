package com.oxxo.spin.comisionvariable.infrastructure.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

/** Definicion global de OpenAPI + esquema de seguridad Keycloak (Bearer JWT). */
@OpenAPIDefinition(
        info = @Info(
                title = "SPIN - Comision Variable API",
                version = "1.0.0",
                description = "Consulta de comision variable desde punto de venta. "
                        + "Protegido por Keycloak (realm de aplicaciones).",
                contact = @Contact(name = "Ingenieria de Soluciones - OXXO")))
@SecurityScheme(
        securitySchemeName = "keycloak",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT emitido por Keycloak (realm 'applications')")
public class OpenApiConfig extends Application {
}

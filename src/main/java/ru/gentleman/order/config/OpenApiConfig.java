package ru.gentleman.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String authServerUrl = "http://localhost:9000";

        return new OpenAPI()
                .info(new Info()
                        .title("Gentleman - OrderService")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("spring_auth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Spring Authorization Server flow")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authServerUrl + "/oauth2/authorize")
                                                .tokenUrl(authServerUrl + "/oauth2/token")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "User profile's")
                                                        .addString("email", "User email's")
                                                )
                                        )
                                )
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("spring_auth"));
    }
}
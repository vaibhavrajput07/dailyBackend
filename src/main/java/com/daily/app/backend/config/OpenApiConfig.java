package com.daily.app.backend.config;

import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.security.BusinessScopeArgumentResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    static {
        // BusinessScope is resolved server-side, never sent by clients.
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(BusinessScope.class);
    }

    @Bean
    public OpenAPI dailyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Daily API")
                        .description("Fastest profit and expense tracking API for small businesses")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /** Documents the optional business-selection header on every business-scoped endpoint. */
    @Bean
    public OperationCustomizer businessScopeHeader() {
        return (operation, handlerMethod) -> {
            boolean scoped = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(p -> BusinessScope.class.equals(p.getParameterType()));
            if (scoped) {
                operation.addParametersItem(new HeaderParameter()
                        .name(BusinessScopeArgumentResolver.HEADER)
                        .required(false)
                        .description("Business to operate on; must belong to the caller. Defaults to the login business.")
                        .schema(new IntegerSchema().format("int64")));
            }
            return operation;
        };
    }
}

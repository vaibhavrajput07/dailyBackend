package com.daily.app.backend.config;

import com.daily.app.backend.security.BusinessScopeArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final BusinessScopeArgumentResolver businessScopeArgumentResolver;

    public WebConfig(BusinessScopeArgumentResolver businessScopeArgumentResolver) {
        this.businessScopeArgumentResolver = businessScopeArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(businessScopeArgumentResolver);
    }
}

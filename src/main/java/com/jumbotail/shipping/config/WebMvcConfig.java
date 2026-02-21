package com.jumbotail.shipping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration to handle route redirects
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect the root URL "/" directly to the Swagger UI page
        registry.addRedirectViewController("/", "/swagger-ui/index.html");
    }
}

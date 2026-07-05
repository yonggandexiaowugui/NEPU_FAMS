package xyz.wolegelei.nepu_fams.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/users/**").setViewName("forward:/index.html");
        registry.addViewController("/categories/**").setViewName("forward:/index.html");
        registry.addViewController("/colleges/**").setViewName("forward:/index.html");
        registry.addViewController("/borrow/**").setViewName("forward:/index.html");
        registry.addViewController("/repair/**").setViewName("forward:/index.html");
        registry.addViewController("/scrap/**").setViewName("forward:/index.html");
        registry.addViewController("/inventory/**").setViewName("forward:/index.html");
        registry.addViewController("/statistics/**").setViewName("forward:/index.html");
        registry.addViewController("/logs/**").setViewName("forward:/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

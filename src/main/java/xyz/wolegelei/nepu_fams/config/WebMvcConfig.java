package xyz.wolegelei.nepu_fams.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/user/**").setViewName("forward:/index.html");
        registry.addViewController("/asset/**").setViewName("forward:/index.html");
        registry.addViewController("/borrow/**").setViewName("forward:/index.html");
        registry.addViewController("/repair/**").setViewName("forward:/index.html");
        registry.addViewController("/scrap/**").setViewName("forward:/index.html");
        registry.addViewController("/inventory/**").setViewName("forward:/index.html");
        registry.addViewController("/statistics/**").setViewName("forward:/index.html");
        registry.addViewController("/3d/**").setViewName("forward:/index.html");
        registry.addViewController("/log/**").setViewName("forward:/index.html");
        registry.addViewController("/profile/**").setViewName("forward:/index.html");
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

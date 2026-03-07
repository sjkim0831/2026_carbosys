package egovframework.com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CommonStaticResourceWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Keep existing URL prefixes and serve from unified static assets.
        registry.addResourceHandler("/main/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/admin/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/uat/uia/**").addResourceLocations("classpath:/static/");
    }
}

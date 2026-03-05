package egovframework.com.config;

import egovframework.com.interceptor.AdminMainAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminMainWeb implements WebMvcConfigurer {

    private final AdminMainAuthInterceptor adminMainAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminMainAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/css/**",
                        "/admin/js/**",
                        "/admin/img/**",
                        "/admin/fonts/**",
                        "/admin/**/*.png",
                        "/admin/**/*.jpg",
                        "/admin/**/*.jpeg",
                        "/admin/**/*.gif",
                        "/admin/**/*.svg",
                        "/admin/**/*.woff",
                        "/admin/**/*.woff2",
                        "/admin/**/*.ttf",
                        "/admin/**/*.otf",
                        "/admin/**/*.eot",
                        "/admin/**/*.ico"
                );
    }
}

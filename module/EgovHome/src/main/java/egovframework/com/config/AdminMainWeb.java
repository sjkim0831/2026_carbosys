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
                .addPathPatterns("/adminmain/**")
                .excludePathPatterns(
                        "/adminmain/css/**",
                        "/adminmain/js/**",
                        "/adminmain/img/**",
                        "/adminmain/fonts/**",
                        "/adminmain/**/*.png",
                        "/adminmain/**/*.jpg",
                        "/adminmain/**/*.jpeg",
                        "/adminmain/**/*.gif",
                        "/adminmain/**/*.svg",
                        "/adminmain/**/*.woff",
                        "/adminmain/**/*.woff2",
                        "/adminmain/**/*.ttf",
                        "/adminmain/**/*.otf",
                        "/adminmain/**/*.eot",
                        "/adminmain/**/*.ico"
                );
    }
}

package egovframework.com.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminMainRouteConfig {

    @Bean
    public RouteLocator adminMainMergedRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("admin-login-proxy",
                        r -> r.order(-2000)
                                .path("/admin/login/**")
                                .uri("http://localhost:18000"))
                .route("adminmain-merged-fixed",
                        r -> r.order(-1000)
                                .path("/adminmain", "/adminmain/**")
                                .uri("http://localhost:18000"))
                .build();
    }
}

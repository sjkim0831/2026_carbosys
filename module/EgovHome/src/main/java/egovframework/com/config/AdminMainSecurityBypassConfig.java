package egovframework.com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
public class AdminMainSecurityBypassConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain adminMainSecurityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/adminmain"),
                new AntPathRequestMatcher("/adminmain/**")))
                .authorizeRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf().disable();
        return http.build();
    }
}

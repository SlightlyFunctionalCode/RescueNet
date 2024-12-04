package org.estg.ipp.pt.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("SecurityConfig loaded");
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF if not needed
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Allow all requests
                .httpBasic(AbstractHttpConfigurer::disable) // Disable HTTP Basic authentication
                .formLogin(AbstractHttpConfigurer::disable); // Disable form-based login

        return http.build();
    }
}

package org.estg.ipp.pt.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A classe {@code SecurityConfig} configura as regras de segurança para a aplicação.
 *
 * <p>Esta configuração desativa a proteção CSRF, desabilita a autenticação básica HTTP e o login de formulário,
 * permitindo que todas as requisições sejam acessadas sem necessidade de autenticação.</p>
 *
 * <p>O método {@code securityFilterChain} define as configurações de segurança para a aplicação,
 * incluindo o desabilitar de CSRF e autenticação, permitindo o acesso irrestrito.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define a configuração de segurança para a aplicação, desabilitando CSRF,
     * a autenticação básica HTTP e o login de formulário.
     *
     * <p>Este método cria um {@code SecurityFilterChain} que permite o acesso irrestrito
     * a todas as requisições e desativa a autenticação básica e o login de formulário.</p>
     *
     * @param http a instância do {@link HttpSecurity} usada para configurar a segurança.
     * @return o {@link SecurityFilterChain} configurado.
     * @throws Exception se ocorrer um erro ao configurar a segurança.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("SecurityConfig loaded");
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}

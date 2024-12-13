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
 * <p>Esta configuração desativa a proteção CSRF, a autenticação básica HTTP e o formulário de login,
 * permitindo que todas as requisições sejam acedidas sem a necessidade de autenticação.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define a configuração de segurança para a aplicação, desativa o CSRF,
     * a autenticação básica HTTP e o formulário login.
     *
     * <p>Este método cria um {@code SecurityFilterChain} que permite o acesso irrestrito
     * a todas as requisições e desativa a autenticação básica e o login de formulário.</p>
     *
     * <p>Isto é feito para que o {@code LogController} possa ser acessado sem autenticação, de forma a permitir o acesso
     * pelo browser do pdf gerado com os logs</p>
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

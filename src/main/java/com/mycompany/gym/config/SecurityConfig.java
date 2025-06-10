package com.mycompany.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.mycompany.gym.service.CustomUserDetailsService;

/**
 * SecurityConfig adaptado para el gimnasio.
 * Usamos el estilo "component-based" con SecurityFilterChain en lugar de WebSecurityConfigurerAdapter.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService uds) {
        this.userDetailsService = uds;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .authorizeRequests(auth -> auth
         // Login/Register y consola H2
         .antMatchers("/api/auth/login","/api/auth/register","/h2-console/**").permitAll()
         // Solo ADMIN para endpoints de administrador
         .antMatchers("/api/activities/admin/**").hasRole("ADMIN")
         // El resto de /api (incluye GET /api/activities) lo dejamos pasar
         .antMatchers("/api/**").permitAll()
         // Recursos estáticos y páginas
         .antMatchers("/login.html","/admin.html","/monitor.html","/activities.html",
                      "/js/**","/css/**").permitAll()
         // Cualquier otro request
         .anyRequest().permitAll()
      )
      .headers(h -> h.frameOptions().disable())
      .formLogin().disable()
      .httpBasic().disable();
    return http.build();
}





    /**
     * Exponemos el AuthenticationManager para que, si en el futuro
     * queremos invocar manualmente el login desde un controlador, lo podamos inyectar.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}

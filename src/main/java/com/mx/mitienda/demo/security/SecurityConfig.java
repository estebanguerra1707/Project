package com.mx.mitienda.demo.security;
import com.mx.mitienda.util.Utils;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioService usuarioService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                           request -> Utils.isPublic(request.getRequestURI())).permitAll()
                        .requestMatchers("/compras/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/ventas/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/proveedores/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/clientes/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/productos/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/sucursales/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/inventario/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/business-types/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/categorias/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/producto-detalle/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/pdf/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/pdf-sender/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/reportes/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/historial/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .requestMatchers("/dashboard/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name())
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, excep) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(usuarioService).passwordEncoder(encoder);
        return builder.build();
    }
}

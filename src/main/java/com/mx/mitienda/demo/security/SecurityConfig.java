package com.mx.mitienda.demo.security;

import com.mx.mitienda.util.Utils;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
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
    private final org.springframework.beans.factory.ObjectProvider<ActuatorApiKeyFilter> actuatorFilterProvider;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<ActuatorApiKeyFilter> actuatorFilterProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator: health abierto, demás con rol ACTUATOR
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ACTUATOR")
                        // Públicos según tu helper
                        .requestMatchers(request -> Utils.isPublic(request.getRequestURI())).permitAll()
                        // Rutas protegidas por roles
                        .requestMatchers("/compras/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/ventas/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/proveedores/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/clientes/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/productos/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/sucursales/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/inventario/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/business-types/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/categorias/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/producto-detalle/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/pdf/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/pdf-sender/**").permitAll()
                        .requestMatchers("/reportes/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/historial/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .requestMatchers("/dashboard/**").hasAnyRole(Rol.ADMIN.name(), Rol.VENDOR.name(), Rol.SUPER_ADMIN.name())
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, excep) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado")
                ));

        // Añade ActuatorApiKeyFilter SOLO si existe un bean registrado
        ActuatorApiKeyFilter act = actuatorFilterProvider.getIfAvailable();
        if (act != null) {
            http.addFilterBefore(act, UsernamePasswordAuthenticationFilter.class);
        }
        // JWT siempre
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(usuarioService).passwordEncoder(encoder);
        return builder.build();
    }
}

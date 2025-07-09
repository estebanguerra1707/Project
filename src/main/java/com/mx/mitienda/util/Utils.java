package com.mx.mitienda.util;

import java.util.List;

public class Utils {

    public static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui",
            "/swagger-ui/**",
            "/swagger-ui/",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/v3/api-docs/swagger-config",
            "/api-docs",
            "/api-docs/"
    );
    public static boolean isPublic(String path) {
        //Verifica si algún elemento del stream cumple una condición
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    public static final String REFACCIONARIA_CODE = "REFACCIONARIA";
    public static final String VENTA_CODE ="venta";
    public static final String COMPRA_CODE = "compra";


}

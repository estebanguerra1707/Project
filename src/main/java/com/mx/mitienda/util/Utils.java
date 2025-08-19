package com.mx.mitienda.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(23, 59, 59, 999_000_000); // hasta las 23:59:59.999
    }

    public static LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay(); // 00:00:00.000
    }

    /**
     * Genera un mapa con todas las fechas entre 'desde' y 'hasta' (inclusive)
     * prellenadas con 0L.
     */
    public static Map<LocalDate, Long> prellenarDiasConCero(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        Map<LocalDate, Long> out = new LinkedHashMap<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            out.put(d, 0L);
        }
        return out;
    }

    /**
     * Genera un mapa con todos los lunes de las semanas entre 'desde' y 'hasta'
     * prellenados con 0L.
     * Ejemplo: si desde es miércoles, ajusta al lunes de esa semana.
     */
    public static Map<LocalDate, Long> prellenarSemanasConCero(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        Map<LocalDate, Long> out = new LinkedHashMap<>();
        LocalDate firstMonday = desde.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastMonday = hasta.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (LocalDate d = firstMonday; !d.isAfter(lastMonday); d = d.plusWeeks(1)) {
            out.put(d, 0L);
        }
        return out;
    }

    /**
     * Genera un mapa con todos los YearMonth (año-mes) entre 'desde' y 'hasta'
     * prellenados con 0L.
     */
    public static Map<YearMonth, Long> prellenarMesesConCero(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        Map<YearMonth, Long> out = new LinkedHashMap<>();
        YearMonth startYm = YearMonth.from(desde);
        YearMonth endYm = YearMonth.from(hasta);

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            out.put(ym, 0L);
        }
        return out;
    }

    public static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
    }

    public static LocalDate toLocalDate(Object dbValue) {
        if (dbValue == null) return null;
        if (dbValue instanceof java.time.LocalDate ld) return ld;
        if (dbValue instanceof java.time.LocalDateTime ldt) return ldt.toLocalDate();
        if (dbValue instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (dbValue instanceof java.sql.Date sd) return sd.toLocalDate();
        throw new IllegalArgumentException("Tipo no soportado para fecha: " + dbValue.getClass());
    }
    public static YearMonth toYearMonth(Object dbValue) {
        LocalDate d = toLocalDate(dbValue);
        return YearMonth.from(d);
    }
}

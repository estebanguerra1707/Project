package com.mx.mitienda.util;

import java.math.BigDecimal;

public class NumberToWordsConverter {

    private static final String[] UNITS = {
            "", "UN", "DOS", "TRES", "CUATRO", "CINCO",
            "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
            "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
            "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE"
    };

    private static final String[] TENS = {
            "", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA",
            "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };

    private static final String[] HUNDREDS = {
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS",
            "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS",
            "NOVECIENTOS"
    };

    public static String convert(BigDecimal amount) {
        int pesos = amount.intValue();
        int cents = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String pesosText = convertNumber(pesos) + (pesos == 1 ? " PESO" : " PESOS");
        String centsText = cents > 0 ? " CON " + convertNumber(cents) + " CENTAVOS" : "";

        if (pesos == 0) {
            pesosText = "CERO PESOS";
        }

        return (pesosText + centsText).toUpperCase();
    }


    private static String convertNumber(int number) {
        if (number == 0) {
            return "CERO";
        } else if (number <= 20) {
            return UNITS[number];
        } else if (number < 100) {
            int tens = number / 10;
            int units = number % 10;
            return TENS[tens] + (units > 0 ? " Y " + UNITS[units] : "");
        } else if (number < 1000) {
            int hundreds = number / 100;
            int remainder = number % 100;
            if (number == 100) {
                return "CIEN";
            }
            return HUNDREDS[hundreds] + " " + convertNumber(remainder);
        } else if (number < 1_000_000) {
            int thousands = number / 1000;
            int remainder = number % 1000;
            String thousandsText = (thousands == 1) ? "MIL" : convertNumber(thousands) + " MIL";
            return thousandsText + " " + convertNumber(remainder);
        } else if (number < 1_000_000_000) {
            int millions = number / 1_000_000;
            int remainder = number % 1_000_000;
            String millionsText = (millions == 1) ? "UN MILLÓN" : convertNumber(millions) + " MILLONES";
            return millionsText + " " + convertNumber(remainder);
        } else {
            return "NÚMERO DEMASIADO GRANDE";
        }
    }
}

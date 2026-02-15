package com.mx.mitienda.util.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnidadMedida {

    PIEZA("pz", "Pieza", false),
    KILOGRAMO("kg", "Kilogramo", true),
    GRAMO("g", "Gramo", true),
    LITRO("L", "Litro", true),
    MILILITRO("mL", "Mililitro", true),
    METRO("m", "Metro", true),
    CENTIMETRO("cm", "Centímetro", true);

    private final String abbr;
    private final String name;
    private final boolean permiteDecimales;
}

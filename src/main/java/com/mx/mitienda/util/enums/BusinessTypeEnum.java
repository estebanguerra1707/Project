package com.mx.mitienda.util.enums;

public enum BusinessTypeEnum {
    REFACCIONARIA(1L),
    FARMACIA(2L),
    ABARROTES(3L),
    TLAPALERIA(4L),
    PAPELERIA(5L);

    private final Long id;

    BusinessTypeEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static BusinessTypeEnum fromId(Long id) {
        for (BusinessTypeEnum bt : values()) {
            if (bt.id.equals(id)) {
                return bt;
            }
        }
        throw new IllegalArgumentException("BusinessType inválido: " + id);
    }
}

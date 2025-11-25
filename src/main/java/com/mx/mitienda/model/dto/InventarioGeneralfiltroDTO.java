package com.mx.mitienda.model.dto;

import lombok.Data;

@Data
public class InventarioGeneralfiltroDTO {
    Long branchId;
    Long businessTypeId;
    String q;
    boolean onlyCritical;
}

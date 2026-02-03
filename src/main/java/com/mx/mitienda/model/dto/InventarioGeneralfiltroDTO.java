package com.mx.mitienda.model.dto;

import com.mx.mitienda.util.enums.InventarioOwnerType;
import lombok.Data;

@Data
public class InventarioGeneralfiltroDTO {
    Long branchId;
    Long businessTypeId;
    String q;
    boolean onlyCritical;
    private InventarioOwnerType ownerType;

}

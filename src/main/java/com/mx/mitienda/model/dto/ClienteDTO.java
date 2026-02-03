package com.mx.mitienda.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClienteDTO {
    private String name;
    private String contact;
    private String email;
    private String phone;
    private Long branchId;
    private List<Long> branchIds;
}

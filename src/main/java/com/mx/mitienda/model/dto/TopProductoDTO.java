package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class TopProductoDTO {

    private String productName;
    private Long totalQuantity;
    private BigDecimal total;
    private LocalDateTime saleDate;
    private String username;
    private String branchName;

}

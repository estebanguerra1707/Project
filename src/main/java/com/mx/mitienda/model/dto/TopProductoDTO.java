package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductoDTO {

    private String productName;
    private Long totalQuantity;
    private LocalDateTime saleDate;
    private String username;
    private String branchName;

}

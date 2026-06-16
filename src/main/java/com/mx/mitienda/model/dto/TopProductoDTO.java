package com.mx.mitienda.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductoDTO {

    private String productName;
    private BigDecimal totalQuantity;
    private LocalDateTime ultimaVenta;
    private String categoria;
    private String tipoNegocio;
    private LocalDateTime saleDate;
    private String username;
    private String branchName;

    private Long productId;
    private BigDecimal totalIncome;
    private Long userId;
    private Long salesCount;
    private List<UsuarioVentaResumenDTO> usuarios;
    private BigDecimal netProfit;
    public TopProductoDTO(String productName, BigDecimal totalQuantity) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
    }

    public TopProductoDTO(Long productId, String productName, BigDecimal totalQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
    }

    public TopProductoDTO(
            String productName,
            BigDecimal totalQuantity,
            BigDecimal totalIncome,
            LocalDateTime saleDate,
            String username,
            String branchName
    ) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalIncome = totalIncome;
        this.saleDate = saleDate;
        this.username = username;
        this.branchName = branchName;
    }

    public TopProductoDTO(
            String productName,
            BigDecimal totalQuantity,
            LocalDateTime ultimaVenta,
            String categoria,
            String tipoNegocio,
            String username,
            String branchName
    ) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.ultimaVenta = ultimaVenta;
        this.categoria = categoria;
        this.tipoNegocio = tipoNegocio;
        this.username = username;
        this.branchName = branchName;
    }

    public TopProductoDTO(
            String productName,
            BigDecimal totalQuantity,
            BigDecimal totalIncome,
            BigDecimal netProfit,
            LocalDateTime ultimaVenta,
            String categoria,
            String tipoNegocio,
            Long userId,
            String username,
            String branchName,
            Long salesCount
    ) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalIncome = totalIncome;
        this.netProfit = netProfit;
        this.ultimaVenta = ultimaVenta;
        this.categoria = categoria;
        this.tipoNegocio = tipoNegocio;
        this.userId = userId;
        this.username = username;
        this.branchName = branchName;
        this.salesCount = salesCount;
    }



}

package com.mx.mitienda.controller;

import com.mx.mitienda.model.dto.TopProductoDTO;
import com.mx.mitienda.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class DashBoardController {
    private final IDashboardService dashboardService;

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductoDTO>> getTopProducts(
            @RequestParam String groupBy,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime end){
      List<TopProductoDTO> responseList = dashboardService.getTopProductos(groupBy, start, end);
      return ResponseEntity.ok(responseList);
    }
}

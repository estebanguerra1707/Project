package com.mx.mitienda.controller;

import com.mx.mitienda.service.IGeneratePdfService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class GeneratePdfController {

    private final IGeneratePdfService generatePdfService;

    @GetMapping("/{type}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<byte[]> generarPdf(
            @PathVariable String type,
            @PathVariable Long id,   @RequestParam(defaultValue = "false") Boolean isPrinted) {
        byte[] pdf = generatePdfService.generatePdf(type, id, isPrinted);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=" + type + "_ticket.pdf")
                .body(pdf);
    }
}

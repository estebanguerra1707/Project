package com.mx.mitienda.controller;

import com.mailjet.client.errors.MailjetException;
import com.mx.mitienda.model.dto.EnvioCorreoDTO;
import com.mx.mitienda.service.IGeneratePdfService;
import com.mx.mitienda.service.MailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/pdf-sender/")
@RequiredArgsConstructor
public class EnvioCorreoPdfController {

    private final IGeneratePdfService generatePdfService;
    private final MailService mailService;

    @GetMapping("/{transactionType}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Tag(name = " ENVIO DE CORREO", description = "Operaciones relacionadas con ENVIO DE CORREO")

    public ResponseEntity<String> sendTransactionPdfByEmail(@PathVariable Long id, @PathVariable String transactionType,
                                                            @RequestBody EnvioCorreoDTO envioCorreoDTO, @RequestParam(defaultValue = "false") boolean isPrinted) throws MailjetException {
        byte[] pdfBytes = generatePdfService.generatePdf(transactionType, id, isPrinted);

        mailService.sendPDFEmail(envioCorreoDTO.getEmailList(),"Cliente",
                "Comprobante de " + transactionType,
                "<p>Adjunto encontrarás tu ticket de "+transactionType+".</p>",
                pdfBytes,
                transactionType + "_ticket.pdf");

        return ResponseEntity.ok("Correo enviado con PDF adjunto");
    }
}

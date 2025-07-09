package com.mx.mitienda.service;

public interface IGeneratePdfService {
    public byte[] generatePdf(String type, Long id, boolean isPrinted);

}

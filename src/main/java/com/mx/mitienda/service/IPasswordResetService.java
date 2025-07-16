package com.mx.mitienda.service;

public interface IPasswordResetService {
    void createToken(String email);
    void resetPassword(String token, String newPassword);}

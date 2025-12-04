package com.mx.mitienda.service;

public interface IPasswordResetService {
    void createToken(String email, String ip);
    void resetPassword(String token, String newPassword);}

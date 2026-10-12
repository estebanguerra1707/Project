package com.mx.mitienda.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class PingController {
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}

package com.mx.mitienda.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatSessionService {

    // Almacena el historial por número de teléfono: Clave (teléfono), Valor (lista de mensajes)
    private final ConcurrentHashMap<String, List<String>> sessions = new ConcurrentHashMap<>();

    public void addMessage(String phoneNumber, String sender, String message) {
        sessions.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(sender + ": " + message);

        // Opcional: mantener solo los últimos 10 mensajes para que el prompt no crezca demasiado
        List<String> history = sessions.get(phoneNumber);
        if (history.size() > 10) {
            history.remove(0);
        }
    }

    public List<String> getHistory(String phoneNumber) {
        return sessions.getOrDefault(phoneNumber, new ArrayList<>());
    }

    public void clearSession(String phoneNumber) {
        sessions.remove(phoneNumber);
    }
}
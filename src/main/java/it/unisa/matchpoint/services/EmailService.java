package it.unisa.matchpoint.services;

public interface EmailService {
    void inviaEmail(String destinatario, String oggetto, String corpo);
}


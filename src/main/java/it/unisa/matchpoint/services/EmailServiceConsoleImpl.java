package it.unisa.matchpoint.services;

import org.springframework.stereotype.Service;

@Service
public class EmailServiceConsoleImpl implements EmailService {
    @Override
    public void inviaEmail(String destinatario, String oggetto, String corpo) {
        // Qui simuli l'invio stampando in console.
        // In un progetto reale, qui useresti JavaMailSender.
        System.out.println("--- SIMULAZIONE INVIO EMAIL ---");
        System.out.println("A: " + destinatario);
        System.out.println("Oggetto: " + oggetto);
        System.out.println("Messaggio: " + corpo);
        System.out.println("-------------------------------");
    }
}


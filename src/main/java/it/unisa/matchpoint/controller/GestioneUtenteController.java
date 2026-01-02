package it.unisa.matchpoint.controller;

import it.unisa.matchpoint.dto.UtenteDTO;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.services.GestioneUtentiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utenti")
public class GestioneUtenteController {

    @Autowired
    private GestioneUtentiService utenteService;

    // SC_1: Registrazione Utente
    @PostMapping("/registrazione")
    public ResponseEntity<?> registraUtente(@RequestBody UtenteDTO utenteDTO) {
        try {
            UtenteRegistrato nuovoUtente = utenteService.registraUtente(utenteDTO);
            return ResponseEntity.ok(nuovoUtente);
        } catch (IllegalArgumentException e) {
            // Restituisce 400 Bad Request se i controlli del Service falliscono (es. password diverse)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // SC_2: Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            UtenteRegistrato utente = utenteService.login(email, password);
            return ResponseEntity.ok(utente);
        } catch (IllegalArgumentException e) {
            // Restituisce 401 Unauthorized se le credenziali sono errate
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
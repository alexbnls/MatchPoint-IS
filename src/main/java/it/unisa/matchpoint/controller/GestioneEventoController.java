package it.unisa.matchpoint.controller;

import it.unisa.matchpoint.dto.EventoDTO;
import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.services.GestioneEventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventi")
public class GestioneEventoController {

    @Autowired
    private GestioneEventoService eventoService;

    // UC_4: Creazione Evento
    @PostMapping("/crea")
    public ResponseEntity<?> creaEvento(@RequestBody EventoDTO eventoDTO, @RequestParam String emailOrganizzatore) {
        try {
            EventoSportivo evento = eventoService.creaEvento(eventoDTO, emailOrganizzatore);
            return ResponseEntity.ok(evento);
        } catch (IllegalArgumentException e) {
            // Cattura gli errori del Service (es. "Data nel passato") e risponde con 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // REQ7: Annullamento Evento
    @PostMapping("/annulla/{id}")
    public ResponseEntity<?> annullaEvento(@PathVariable Integer id, @RequestParam String emailRichiedente) {
        try {
            eventoService.annullaEvento(id, emailRichiedente);
            return ResponseEntity.ok("Evento annullato con successo. Notifiche inviate.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 Forbidden
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package it.unisa.matchpoint.controller;

import it.unisa.matchpoint.dto.RatingDTO;
import it.unisa.matchpoint.model.Feedback;
import it.unisa.matchpoint.services.GestioneValutazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/valutazione")
public class GestioneValutazioneController {

    @Autowired
    private GestioneValutazioneService valutazioneService;

    /**
     * Riceve un JSON complesso con i dati dell'evento, degli utenti e i voti.
     */
    @PostMapping("/lascia-feedback")
    public ResponseEntity<?> lasciaFeedbac(@RequestBody RatingDTO ratingDTO, @RequestParam Integer idEvento, @RequestParam String emailValutatore, @RequestParam String emailValutato) {
        try {

            // 1. Validazione preliminare dei campi obbligatori (se nulli, inutile chiamare il service)
            if (idEvento == null || emailValutatore == null || emailValutato == null) {
                return ResponseEntity.badRequest().body(Map.of("errore", "Dati mancanti (ID Evento o Email)."));
            }

            // 2. Chiamata al Service
            Feedback feedback = valutazioneService.lasciaFeedback(idEvento, emailValutatore, emailValutato, ratingDTO);

            // 3. Risposta 200 OK
            return ResponseEntity.ok(Map.of(
                    "messaggio", "Feedback inviato con successo!",
                    "idFeedback", feedback.getId()
            ));

        } catch (IllegalArgumentException e) {
            // Gestisce errori di validazione (es. voti fuori range, utente non iscritto, ecc.)
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("errore", "Errore interno del server."));
        }
    }

}
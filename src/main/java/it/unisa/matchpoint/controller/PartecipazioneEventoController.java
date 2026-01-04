package it.unisa.matchpoint.controller;

import it.unisa.matchpoint.model.Iscrizione;
import it.unisa.matchpoint.services.PartecipazioneEventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/partecipazione")
public class PartecipazioneEventoController {

    @Autowired
    private PartecipazioneEventoService partecipazioneService;

    @PostMapping("/iscriviti")
    public ResponseEntity<?> iscriviti(@RequestBody Map<String, Object> payload) {
        try {
            // Estraiamo i dati dal corpo della richiesta JSON
            Integer idEvento = (Integer) payload.get("idEvento");
            String emailUtente = (String) payload.get("emailUtente");

            if (idEvento == null || emailUtente == null) {
                return ResponseEntity.badRequest().body("Dati mancanti: idEvento e emailUtente sono richiesti.");
            }

            // Chiamata al Service (Business Logic)
            Iscrizione iscrizione = partecipazioneService.iscriviUtente(idEvento, emailUtente);

            return ResponseEntity.ok(Map.of(
                    "messaggio", "Iscrizione completata con successo!",
                    "idIscrizione", iscrizione.getId(),
                    "statoEvento", iscrizione.getEvento().getStato()
            ));

        } catch (IllegalStateException e) {
            // restituiamo 400 Bad Request con il messaggio d'errore del service.
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        } catch (Exception e) {
            // Per errori imprevisti, 500 Internal Server Error
            return ResponseEntity.internalServerError().body(Map.of("errore", "Errore interno del sistema."));
        }
    }
}

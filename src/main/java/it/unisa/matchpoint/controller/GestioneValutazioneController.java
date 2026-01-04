package it.unisa.matchpoint.controller;

import it.unisa.matchpoint.dto.RatingDTO;
import it.unisa.matchpoint.model.Feedback;
import it.unisa.matchpoint.services.GestioneValutazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> lasciaFeedback(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Estrazione Parametri dalla richiesta JSON
            // "payload.get" restituisce Object, quindi dobbiamo fare il casting
            Integer idEvento = (Integer) payload.get("idEvento");
            String emailValutatore = (String) payload.get("emailValutatore");
            String emailValutato = (String) payload.get("emailValutato");

            // Estrazione dei voti (gestendo il fatto che JSON potrebbe mandarli come Integer o Double)
            // Usiamo un helper o convertiamo a Double in modo sicuro
            Double abilita = convertToDouble(payload.get("abilita"));
            Double affidabilita = convertToDouble(payload.get("affidabilita"));
            Double sportivita = convertToDouble(payload.get("sportivita"));
            String descrizione = (String) payload.get("descrizione");

            // 2. Creazione del DTO
            RatingDTO ratingDTO = new RatingDTO(abilita, affidabilita, sportivita, descrizione);

            // 3. Validazione preliminare dei campi obbligatori (se nulli, inutile chiamare il service)
            if (idEvento == null || emailValutatore == null || emailValutato == null) {
                return ResponseEntity.badRequest().body(Map.of("errore", "Dati mancanti (ID Evento o Email)."));
            }

            // 4. Chiamata al Service
            Feedback feedback = valutazioneService.lasciaFeedback(idEvento, emailValutatore, emailValutato, ratingDTO);

            // 5. Risposta 200 OK
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

    // Metodo helper per gestire la conversione da JSON number a Double in modo sicuro
    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Double) return (Double) value;
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
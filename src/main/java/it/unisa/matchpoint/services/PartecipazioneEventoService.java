package it.unisa.matchpoint.services;

import it.unisa.matchpoint.model.*;
import it.unisa.matchpoint.repository.EventoRepository;
import it.unisa.matchpoint.repository.IscrizioneRepository;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PartecipazioneEventoService {

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    /**
     * Gestisce atomicamente il controllo posti e il salvataggio.
     */
    @Transactional
    public Iscrizione iscriviUtente(Integer idEvento, String emailUtente) {

        // Recupero le entità (se non esistono, errore generico o gestione specifica)
        EventoSportivo evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento non trovato"));

        UtenteRegistrato utente = utenteRepository.findById(emailUtente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // 1. Controllo Stato Iscrizione [IS] - TC_UC3_1
        // Verifica se l'utente è già iscritto
        if (iscrizioneRepository.existsByEventoIdAndUtenteEmail(idEvento, emailUtente)) {
            throw new IllegalStateException("Errato: Utente già iscritto all'evento");
        }

        // 2. Controllo Disponibilità Posti [DP] - TC_UC3_2
        if (evento.getNPartAttuali() >= evento.getNPartMax()) {
            throw new IllegalStateException("Errato: Posti esauriti");
        }

        // 3. Controllo Stato Evento [SE] - TC_UC3_3
        // L'evento deve essere in stato "IN_ATTESA..." o "CREATO", non terminato o annullato
        if (evento.getStato() == StatoEvento.TERMINATO || evento.getStato() == StatoEvento.ANNULLATO) {
            throw new IllegalStateException("Errato: Stato evento non valido");
        }

        // 4. Controllo Validità Temporale [VT] - TC_UC3_4
        if (evento.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Errato: Data evento passata");
        }

        // --- Se tutti i controlli passano, eseguiamo l'iscrizione (Transazione Atomica) ---

        // A. Incrementa il contatore dei partecipanti nell'evento
        evento.setNPartAttuali(evento.getNPartAttuali() + 1);

        // Logica Extra: Se si raggiunge il massimo, cambiamo stato? (Opzionale, ma consigliato dal REQ6)
        if (evento.getNPartAttuali().equals(evento.getNPartMax())) {
            evento.setStato(StatoEvento.CONFERMATO);
        }
        eventoRepository.save(evento);

        // B. Crea e salva l'oggetto Iscrizione
        Iscrizione nuovaIscrizione = new Iscrizione();
        nuovaIscrizione.setEvento(evento);
        nuovaIscrizione.setUtente(utente); // Assumendo che Iscrizione abbia il campo Utente (FK)
        nuovaIscrizione.setDataIscrizione(LocalDateTime.now());
        nuovaIscrizione.setStato(StatoIscrizione.CONFERMATO);

        return iscrizioneRepository.save(nuovaIscrizione);
    }
}
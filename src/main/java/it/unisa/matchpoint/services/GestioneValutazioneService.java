package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.RatingDTO;
import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.model.Feedback;
import it.unisa.matchpoint.model.StatoEvento;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.EventoRepository;
import it.unisa.matchpoint.repository.FeedbackRepository;
import it.unisa.matchpoint.repository.IscrizioneRepository;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GestioneValutazioneService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    /**
     * Gestisce l'inserimento di un feedback.
     */
    @Transactional
    public Feedback lasciaFeedback(Integer idEvento, String emailValutatore, String emailValutato, RatingDTO ratingDTO) {

        // --- 1. VALIDAZIONE RATING A SE STESSO (TC_UC2_1) ---
        if (emailValutatore.equals(emailValutato)) {
            throw new IllegalArgumentException("Non puoi valutare te stesso");
        }

        // --- 2. RECUPERO ENTITY ---
        EventoSportivo evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento non trovato"));

        UtenteRegistrato valutatore = utenteRepository.findById(emailValutatore)
                .orElseThrow(() -> new IllegalArgumentException("Valutatore non trovato"));

        UtenteRegistrato valutato = utenteRepository.findById(emailValutato)
                .orElseThrow(() -> new IllegalArgumentException("Utente valutato non trovato"));

        // --- 3. VALIDAZIONE TEMPORALE ---
        if (evento.getDataOra().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Non puoi lasciare feedback per un evento futuro");
        }

        if (evento.getStato() == StatoEvento.ANNULLATO) {
            throw new IllegalArgumentException("Impossibile lasciare feedback: L'evento è stato annullato.");
        }

        // --- 4. VALIDAZIONE PARTECIPAZIONE ---
        boolean isValutatoreIscritto = iscrizioneRepository.existsByEventoIdAndUtenteEmail(idEvento, emailValutatore);
        boolean isValutatoIscritto = iscrizioneRepository.existsByEventoIdAndUtenteEmail(idEvento, emailValutato);

        if (!isValutatoreIscritto || !isValutatoIscritto) {
            throw new IllegalArgumentException("Entrambi gli utenti devono aver partecipato all'evento");
        }

        // --- 5. VALIDAZIONE UNICITÀ (TC_UC2_4) ---
        if (feedbackRepository.existsByEventoIdAndValutatoreEmailAndValutatoEmail(idEvento, emailValutatore, emailValutato)) {
            throw new IllegalArgumentException("Hai già valutato questo utente per questo evento");
        }

        // --- 6. VALIDAZIONE RANGE (TC_UC2_3) ---
        // Controlliamo i Double primitivi
        if (!isPunteggioValido(ratingDTO.getAbilita()) ||
                !isPunteggioValido(ratingDTO.getAffidabilita()) ||
                !isPunteggioValido(ratingDTO.getSportivita())) {
            throw new IllegalArgumentException("I punteggi devono essere compresi tra 1 e 5");
        }

        // --- 7. SALVATAGGIO FEEDBACK ---
        Feedback feedback = new Feedback();
        feedback.setEvento(evento);
        feedback.setValutatore(valutatore);
        feedback.setValutato(valutato);

        feedback.setPunteggioAbilita(ratingDTO.getAbilita());
        feedback.setPunteggioAffidabilita(ratingDTO.getAffidabilita());
        feedback.setPunteggioSportivita(ratingDTO.getSportivita());
        feedback.setDescrizione(ratingDTO.getDescrizione());

        Feedback feedbackSalvato = feedbackRepository.save(feedback);

        // --- 8. AGGIORNAMENTO MEDIA UTENTE ---
        aggiornaMediaUtente(valutato, ratingDTO);

        return feedbackSalvato;
    }

    // Helper semplificato per Double
    private boolean isPunteggioValido(Double voto) {
        return voto != null && voto >= 1.0 && voto <= 5.0;
    }

    private void aggiornaMediaUtente(UtenteRegistrato utente, RatingDTO nuoviVoti) {
        // 1. Contiamo quanti feedback ha ricevuto INCLUSO quello appena salvato
        long numeroTotaleFeedback = feedbackRepository.countByValutato(utente);

        // 2. Calcoliamo le nuove medie usando il conteggio
        utente.setRatingAbilita(calcolaMediaPonderata(utente.getRatingAbilita(), nuoviVoti.getAbilita(), numeroTotaleFeedback));
        utente.setRatingAffidabilita(calcolaMediaPonderata(utente.getRatingAffidabilita(), nuoviVoti.getAffidabilita(), numeroTotaleFeedback));
        utente.setRatingSportivita(calcolaMediaPonderata(utente.getRatingSportivita(), nuoviVoti.getSportivita(), numeroTotaleFeedback));

        utenteRepository.save(utente);
    }

    private Double calcolaMediaPonderata(Double mediaAttuale, Double nuovoVoto, long numeroTotale) {
        if (mediaAttuale == null) mediaAttuale = 0.0;

        // Se è il primo feedback in assoluto (numeroTotale = 1)
        if (numeroTotale <= 1) {
            return nuovoVoto;
        }

        double sommaPrecedente = mediaAttuale * (numeroTotale - 1);
        double nuovaSomma = sommaPrecedente + nuovoVoto;

        // Arrotondiamo a 2 cifre decimali per pulizia
        double nuovaMedia = nuovaSomma / numeroTotale;
        return Math.round(nuovaMedia * 100.0) / 100.0;
    }
}
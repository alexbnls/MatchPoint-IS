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
     * Gestisce l'inserimento di un feedback secondo UC_2.
     * Versione con Double.
     */
    @Transactional
    public Feedback lasciaFeedback(Integer idEvento, String emailValutatore, String emailValutato, RatingDTO ratingDTO) {

        // --- 1. VALIDAZIONE SELF-RATING (TC_UC2_1) ---
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

        // Assegnazione diretta dei Double
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
        utente.setRatingAbilita(calcolaNuovaMedia(utente.getRatingAbilita(), nuoviVoti.getAbilita()));
        utente.setRatingAffidabilita(calcolaNuovaMedia(utente.getRatingAffidabilita(), nuoviVoti.getAffidabilita()));
        utente.setRatingSportivita(calcolaNuovaMedia(utente.getRatingSportivita(), nuoviVoti.getSportivita()));

        utenteRepository.save(utente);
    }

    private Double calcolaNuovaMedia(Double mediaAttuale, Double nuovoVoto) {
        // Gestione null safety
        if (mediaAttuale == null) mediaAttuale = 0.0;

        // Se è la prima valutazione (media 0), ritorna direttamente il nuovo voto
        if (mediaAttuale == 0.0) {
            return nuovoVoto;
        }

        // Calcolo media mobile semplice: (Vecchia + Nuova) / 2
        return (mediaAttuale + nuovoVoto) / 2.0;
    }
}
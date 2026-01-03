package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.RatingDTO;
import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.EventoRepository;
import it.unisa.matchpoint.repository.FeedbackRepository;
import it.unisa.matchpoint.repository.IscrizioneRepository;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestioneValutazioneServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private UtenteRepository utenteRepository;
    @Mock
    private EventoRepository eventoRepository;
    @Mock
    private IscrizioneRepository iscrizioneRepository;

    @InjectMocks
    private GestioneValutazioneService service;

    private UtenteRegistrato valutatore;
    private UtenteRegistrato valutato;
    private EventoSportivo evento;
    private RatingDTO ratingDTO;

    @BeforeEach
    void setUp() {
        // Setup dati comuni
        valutatore = new UtenteRegistrato();
        valutatore.setEmail("mario@email.com");

        valutato = new UtenteRegistrato();
        valutato.setEmail("luigi@email.com");
        // Inizializziamo i rating a 0.0 per evitare NullPointerException nei calcoli
        valutato.setRatingAbilita(0.0);
        valutato.setRatingAffidabilita(0.0);
        valutato.setRatingSportivita(0.0);

        evento = new EventoSportivo();
        evento.setId(1);
        // Evento nel passato (valido per feedback)
        evento.setDataOra(LocalDateTime.now().minusDays(1));

        // DTO con i voti (Double)
        ratingDTO = new RatingDTO(
                4.0, // Abilità
                5.0, // Affidabilità
                3.0, // Sportività
                "Buon giocatore"
        );
    }

    // --- TEST CASE: TC_UC2_1 (Self Rating) ---
    @Test
    void lasciaFeedback_StessoUtente_LanciaEccezione() {
        // Tentativo di valutare se stesso
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lasciaFeedback(1, "mario@email.com", "mario@email.com", ratingDTO);
        });

        assertEquals("Non puoi valutare te stesso", ex.getMessage());
    }

    // --- TEST CASE: Evento Futuro (Data non valida) ---
    @Test
    void lasciaFeedback_EventoFuturo_LanciaEccezione() {
        evento.setDataOra(LocalDateTime.now().plusDays(1)); // Domani

        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("mario@email.com")).thenReturn(Optional.of(valutatore));
        when(utenteRepository.findById("luigi@email.com")).thenReturn(Optional.of(valutato));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lasciaFeedback(1, "mario@email.com", "luigi@email.com", ratingDTO);
        });

        assertEquals("Non puoi lasciare feedback per un evento futuro", ex.getMessage());
    }

    // --- TEST CASE: Utenti Non Iscritti ---
    @Test
    void lasciaFeedback_NonIscritti_LanciaEccezione() {
        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("mario@email.com")).thenReturn(Optional.of(valutatore));
        when(utenteRepository.findById("luigi@email.com")).thenReturn(Optional.of(valutato));

        // Simuliamo che il valutatore NON sia iscritto
        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(1, "mario@email.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lasciaFeedback(1, "mario@email.com", "luigi@email.com", ratingDTO);
        });

        assertEquals("Entrambi gli utenti devono aver partecipato all'evento", ex.getMessage());
    }

    // --- TEST CASE: TC_UC2_4 (Feedback Duplicato) ---
    @Test
    void lasciaFeedback_GiaPresente_LanciaEccezione() {
        // Mock: Evento e Utenti esistono
        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("mario@email.com")).thenReturn(Optional.of(valutatore));
        when(utenteRepository.findById("luigi@email.com")).thenReturn(Optional.of(valutato));

        // Mock: Partecipazione OK
        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(anyInt(), anyString())).thenReturn(true);

        // Mock: Feedback GIÀ ESISTENTE -> TRUE
        when(feedbackRepository.existsByEventoIdAndValutatoreAndValutato(1, "mario@email.com", "luigi@email.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lasciaFeedback(1, "mario@email.com", "luigi@email.com", ratingDTO);
        });

        assertEquals("Hai già valutato questo utente per questo evento", ex.getMessage());
    }

    // --- TEST CASE: TC_UC2_3 (Range Punteggi Errato) ---
    @Test
    void lasciaFeedback_PunteggioFuoriRange_LanciaEccezione() {
        // Mock basilari per arrivare al controllo punteggi
        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("mario@email.com")).thenReturn(Optional.of(valutatore));
        when(utenteRepository.findById("luigi@email.com")).thenReturn(Optional.of(valutato));
        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(anyInt(), anyString())).thenReturn(true);
        when(feedbackRepository.existsByEventoIdAndValutatoreAndValutato(anyInt(), anyString(), anyString())).thenReturn(false);

        // Impostiamo un voto invalido (6.0)
        ratingDTO.setAbilita(6.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lasciaFeedback(1, "mario@email.com", "luigi@email.com", ratingDTO);
        });

        assertEquals("I punteggi devono essere compresi tra 1 e 5", ex.getMessage());
    }

    // --- TEST CASE: TC_UC2_OK (Inserimento Valido) ---
    @Test
    void lasciaFeedback_DatiValidi_SalvaCorrettamente() {
        // Mock: Tutto perfetto
        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("mario@email.com")).thenReturn(Optional.of(valutatore));
        when(utenteRepository.findById("luigi@email.com")).thenReturn(Optional.of(valutato));

        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(anyInt(), anyString())).thenReturn(true);
        when(feedbackRepository.existsByEventoIdAndValutatoreAndValutato(anyInt(), anyString(), anyString()))
                .thenReturn(false); // Non esiste ancora

        // Esecuzione
        service.lasciaFeedback(1, "mario@email.com", "luigi@email.com", ratingDTO);

        // Verifica: Il repository save() è stato chiamato per il Feedback?
        verify(feedbackRepository, times(1)).save(any());

        // Verifica: La media utente è stata aggiornata (repository save utente)?
        verify(utenteRepository, times(1)).save(valutato);
    }
}
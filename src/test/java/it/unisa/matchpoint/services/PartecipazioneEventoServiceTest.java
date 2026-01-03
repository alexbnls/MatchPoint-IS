package it.unisa.matchpoint.services;

import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.model.Iscrizione;
import it.unisa.matchpoint.model.StatoEvento;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.EventoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartecipazioneEventoServiceTest {

    @Mock
    private IscrizioneRepository iscrizioneRepository;
    @Mock
    private EventoRepository eventoRepository;
    @Mock
    private UtenteRepository utenteRepository;

    @InjectMocks
    private PartecipazioneEventoService service;

    private EventoSportivo evento;
    private UtenteRegistrato utente;

    @BeforeEach
    void setUp() {
        utente = new UtenteRegistrato();
        utente.setEmail("player@email.com");

        evento = new EventoSportivo();
        evento.setId(1);
        evento.setNPartMax(10);
        evento.setNPartAttuali(5); // C'è posto
        evento.setDataOra(LocalDateTime.now().plusDays(2)); // Futuro
        evento.setStato(StatoEvento.IN_ATTESA_DI_PARTECIPANTI);
    }

    // --- TEST CASE: TC_UC3_2 (Già Iscritto) ---
    @Test
    void confermaPartecipazione_GiaIscritto_LanciaEccezione() {
        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("player@email.com")).thenReturn(Optional.of(utente));

        // Simuliamo che esista già una riga nella tabella Iscrizione
        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(1, "player@email.com")).thenReturn(true);

        // MODIFICA: Ci aspettiamo IllegalStateException, non IllegalArgumentException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.iscriviUtente(1, "player@email.com");
        });

        // Verifica che il messaggio sia quello visto nel log
        assertEquals("Errato: Utente già iscritto all'evento", ex.getMessage());
    }

    // --- TEST CASE: TC_UC3_3 (Evento Pieno) ---
    @Test
    void confermaPartecipazione_EventoPieno_LanciaEccezione() {
        evento.setNPartAttuali(10); // 10 su 10 -> PIENO

        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("player@email.com")).thenReturn(Optional.of(utente));

        // MODIFICA: Ci aspettiamo IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.iscriviUtente(1, "player@email.com");
        });

        // Verifica che il messaggio sia quello visto nel log
        assertEquals("Errato: Posti esauriti", ex.getMessage());
    }

    // --- TEST CASE: TC_UC3_OK (Successo e Cambio Stato) ---
    @Test
    void confermaPartecipazione_PostoLibero_SalvaEAggiornaStato() {
        // ARRANGE: Evento con 9 iscritti su 10. Se mi iscrivo divento il 10°.
        evento.setNPartAttuali(9);
        evento.setNPartMax(10);

        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(utenteRepository.findById("player@email.com")).thenReturn(Optional.of(utente));
        when(iscrizioneRepository.existsByEventoIdAndUtenteEmail(1, "player@email.com")).thenReturn(false);

        // ACT
        service.iscriviUtente(1, "player@email.com");

        // ASSERT
        // 1. Verifichiamo che l'evento sia stato salvato con i nuovi dati
        verify(eventoRepository).save(argThat(evt ->
                evt.getNPartAttuali() == 10 &&       // Contatore incrementato
                        evt.getStato() == StatoEvento.CONFERMATO // Stato cambiato!
        ));

        // 2. Verifichiamo che l'iscrizione sia stata creata
        verify(iscrizioneRepository).save(any(Iscrizione.class));
    }
}
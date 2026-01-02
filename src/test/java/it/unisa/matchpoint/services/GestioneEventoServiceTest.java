package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.EventoDTO;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestioneEventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private IscrizioneRepository iscrizioneRepository;
    @Mock private MappeServiceFacade mappeFacade;

    @InjectMocks
    private GestioneEventoService service;

    private EventoDTO dtoValido;
    private UtenteRegistrato organizzatore;

    @BeforeEach
    void setUp() {
        organizzatore = new UtenteRegistrato();
        organizzatore.setEmail("org@email.com");

        dtoValido = new EventoDTO();
        dtoValido.setSport("Calcetto");
        dtoValido.setLuogo("Campetto Comunale, Salerno");
        dtoValido.setNPartMax(10);
        dtoValido.setDataOra(LocalDateTime.now().plusDays(5)); // Futuro
    }

    // --- TEST CASE: TC_UC4_1 (Data nel passato) ---
    @Test
    void creaEvento_DataPassata_LanciaEccezione() {
        dtoValido.setDataOra(LocalDateTime.now().minusDays(1)); // Ieri

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.creaEvento(dtoValido, "org@email.com");
        });

        // CORREZIONE: Aggiornato con il messaggio reale del Service
        assertEquals("Errato: Data evento nel passato", ex.getMessage());
    }

    // --- TEST CASE: TC_UC4_2 (Partecipanti Insufficienti) ---
    @Test
    void creaEvento_PochiPartecipanti_LanciaEccezione() {
        dtoValido.setNPartMax(1); // Minimo è 2

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.creaEvento(dtoValido, "org@email.com");
        });

        // Verifica anche qui per sicurezza
        assertEquals("Errato: Numero partecipanti insufficiente", ex.getMessage());
    }

    // --- TEST CASE: TC_UC4_3 (Regex Sport Errata) ---
    @Test
    void creaEvento_SportNonValido_LanciaEccezione() {
        dtoValido.setSport("A"); // Troppo corto (min 3)

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.creaEvento(dtoValido, "org@email.com");
        });

        assertEquals("Errato: Nome sport troppo breve o lungo", ex.getMessage());
    }

    // --- TEST CASE: TC_UC4_OK (Creazione Riuscita) ---
    @Test
    void creaEvento_DatiValidi_ChiamaFacadeESalva() {
        // ARRANGE
        when(utenteRepository.findById("org@email.com")).thenReturn(Optional.of(organizzatore));

        // Simuliamo che il Facade restituisca coordinate valide
        Double[] coordinateFittizie = {40.682, 14.768};
        when(mappeFacade.getCoordinate(dtoValido.getLuogo())).thenReturn(coordinateFittizie);

        // ACT
        service.creaEvento(dtoValido, "org@email.com");

        // ASSERT
        verify(mappeFacade).getCoordinate(dtoValido.getLuogo());
        verify(eventoRepository).save(any(EventoSportivo.class));
    }

    // --- TEST CASE: UC_OBSERVER (Annullamento Evento) ---
    @Test
    void annullaEvento_EventoValido_NotificaOsservatori() {
        // ARRANGE
        EventoSportivo evento = new EventoSportivo();
        evento.setId(1);
        evento.setStato(StatoEvento.CONFERMATO);
        evento.setOrganizzatore(organizzatore);
        evento.setSport("Tennis");

        UtenteRegistrato u1 = new UtenteRegistrato(); u1.setEmail("user1@test.com");
        Iscrizione i1 = new Iscrizione(); i1.setUtente(u1);

        List<Iscrizione> osservatori = List.of(i1);

        when(eventoRepository.findById(1)).thenReturn(Optional.of(evento));
        when(iscrizioneRepository.findByEventoId(1)).thenReturn(osservatori);

        // ACT
        service.annullaEvento(1, "org@email.com");

        // ASSERT
        assertEquals(StatoEvento.ANNULLATO, evento.getStato());
        verify(eventoRepository).save(evento);
        verify(iscrizioneRepository).findByEventoId(1);
    }
}
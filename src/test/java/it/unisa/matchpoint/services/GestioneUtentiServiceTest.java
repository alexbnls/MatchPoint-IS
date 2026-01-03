package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.UtenteDTO;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 1. Abilita Mockito
class GestioneUtentiServiceTest {

    @Mock // 2. Crea la "controfigura" del Repository (non parla col DB vero)
    private UtenteRepository utenteRepository;

    @InjectMocks // 3. Inietta il Repository finto dentro il Service vero
    private GestioneUtentiService gestioneUtentiService;

    // Test Case: TC_UC1_4 (Email già in uso)
    @Test
    void registraUtente_EmailGiaEsistente_LanciaEccezione() {
        // --- ARRANGE (Preparazione) ---
        UtenteDTO dto = new UtenteDTO();
        dto.setNome("Mario");
        dto.setCognome("Rossi");
        dto.setEmail("mario.rossi@email.com");
        dto.setPassword("Password123");
        dto.setConfermaPassword("Password123");

        // ISTRUZIONE MOCKITO:
        // "Quando qualcuno chiama existsByEmail con questa mail, rispondi TRUE"
        when(utenteRepository.existsByEmail("mario.rossi@email.com")).thenReturn(true);

        // --- ACT & ASSERT (Azione e Verifica) ---
        // Ci aspettiamo che il service lanci IllegalArgumentException come scritto nel codice
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gestioneUtentiService.registraUtente(dto);
        });

        assertEquals("Errato: Email già in uso", exception.getMessage());

        // Verifica finale: controlliamo che il mock sia stato chiamato davvero una volta
        verify(utenteRepository, times(1)).existsByEmail("mario.rossi@email.com");
        // Verifica che NON abbia provato a salvare nulla (visto che doveva fallire prima)
        verify(utenteRepository, never()).save(any());
    }
}
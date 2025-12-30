package it.unisa.matchpoint.services;


import it.unisa.matchpoint.dto.EventoDTO;
import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.model.StatoEvento;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.EventoRepository;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GestioneEventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;


    @Transactional
    public EventoSportivo creaEvento(EventoDTO eventoDTO, String emailOrganizzatore) {

        // 1. Controllo Validità Temporale [VT] - TC_UC4_1
        if (eventoDTO.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Errato: Data evento nel passato");
        }

        // 2. Controllo Numero Partecipanti [NP] - TC_UC4_2
        if (eventoDTO.getNPartMax() < 2) {
            throw new IllegalArgumentException("Errato: Numero partecipanti insufficiente");
        }

        // 3. Controllo Lunghezza Sport [LS] - TC_UC4_3
        if (!eventoDTO.getSport().matches("^[a-zA-Z\\s]{3,30}$")) {
            throw new IllegalArgumentException("Errato: Nome sport troppo breve o lungo");
        }

        // 4. Controllo Lunghezza Luogo [LL] - TC_UC4_4
        if (!eventoDTO.getLuogo().matches("^[a-zA-Z0-9\\s,.-]{5,100}$")) {
            throw new IllegalArgumentException("Errato: Indirizzo luogo non valido");
        }

        // 5. Controllo Stato Organizzatore [SO] - TC_UC4_
        UtenteRegistrato organizzatore = utenteRepository.findById(emailOrganizzatore)
                .orElseThrow(() -> new IllegalArgumentException("Errato: Organizzatore non valido"));

        // Creazione Entity e settaggio stato iniziale (REQ6)
        EventoSportivo nuovoEvento = new EventoSportivo();
        nuovoEvento.setSport(eventoDTO.getSport());
        nuovoEvento.setDataOra(eventoDTO.getDataOra());
        nuovoEvento.setLuogo(eventoDTO.getLuogo());
        nuovoEvento.setNPartMax(eventoDTO.getNPartMax());
        nuovoEvento.setOrganizzatore(organizzatore);

        // Impostiamo lo stato iniziale come da Statechart (REQ6)
        nuovoEvento.setStato(StatoEvento.IN_ATTESA_DI_PARTECIPANTI);
        nuovoEvento.setNPartAttuali(0);

        return eventoRepository.save(nuovoEvento);
    }

    /**
     * Implementazione REQ7 e Observer Pattern: Annullamento Evento.
     * Quando l'organizzatore annulla, il sistema deve notificare gli iscritti.
     */
    @Transactional
    public void annullaEvento(Integer idEvento, String emailRichiedente) {
        EventoSportivo evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("Evento non trovato"));

        // Verifica che sia l'organizzatore a cancellare (DG4 - Separazione Privilegi)
        if (!evento.getOrganizzatore().getEmail().equals(emailRichiedente)) {
            throw new SecurityException("Non sei autorizzato ad annullare questo evento"); //
        }

        // Cambio di stato (Statechart transition)
        evento.setStato(StatoEvento.ANNULLATO);
        eventoRepository.save(evento);

        // Pattern Observer: Notifica automatica agli iscritti
        // Nota: Questo metodo deve essere presente nell'Entity EventoSportivo
        // evento.notificaOsservatori(); //
    }
}
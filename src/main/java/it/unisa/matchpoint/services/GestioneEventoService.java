package it.unisa.matchpoint.services;


import it.unisa.matchpoint.dto.EventoDTO;
import it.unisa.matchpoint.model.EventoSportivo;
import it.unisa.matchpoint.model.Iscrizione;
import it.unisa.matchpoint.model.StatoEvento;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.EventoRepository;
import it.unisa.matchpoint.repository.IscrizioneRepository;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestioneEventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private MappeServiceFacade mappeFacade; // Il Design Pattern in azione

    @Autowired
    private IscrizioneRepository iscrizioneRepository; // <--- SERVE PER L'OBSERVER!

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


        Double[] coordinate;
        try {
            coordinate = mappeFacade.getCoordinate(eventoDTO.getLuogo());
        } catch (RuntimeException e) {
            // Se il Facade fallisce (indirizzo non trovato dalle API), blocchiamo la creazione
            throw new IllegalArgumentException("Indirizzo non trovato sulle mappe");
        }

        // Creazione Entity e settaggio stato iniziale (REQ6)
        EventoSportivo nuovoEvento = new EventoSportivo();
        nuovoEvento.setSport(eventoDTO.getSport());
        nuovoEvento.setDataOra(eventoDTO.getDataOra());
        nuovoEvento.setLuogo(eventoDTO.getLuogo());
        nuovoEvento.setNPartMax(eventoDTO.getNPartMax());
        nuovoEvento.setOrganizzatore(organizzatore);
        nuovoEvento.setLatitudine(coordinate[0]);
        nuovoEvento.setLongitudine(coordinate[1]);
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

        // Verifica Security
        if (!evento.getOrganizzatore().getEmail().equals(emailRichiedente)) {
            throw new SecurityException("Non sei autorizzato ad annullare questo evento");
        }

        // Verifica Stato
        if (evento.getStato() == StatoEvento.TERMINATO || evento.getStato() == StatoEvento.ANNULLATO) {
            throw new IllegalArgumentException("L'evento non può essere annullato in questo stato");
        }

        // 1. STATE CHANGE (Il Subject cambia stato)
        evento.setStato(StatoEvento.ANNULLATO);
        eventoRepository.save(evento);

        // 2. NOTIFY OBSERVERS (Logica manuale perché siamo in un Service Layer)
        // Recuperiamo la lista degli iscritti (Observer)
        List<Iscrizione> osservatori = iscrizioneRepository.findByEventoId(idEvento);

        for (Iscrizione iscrizione : osservatori) {
            UtenteRegistrato utente = iscrizione.getUtente();
            // Simulazione invio notifica (Email/Push)
            System.out.println("NOTIFICA a " + utente.getEmail() + ": L'evento è stato annullato.");
        }
    }
}
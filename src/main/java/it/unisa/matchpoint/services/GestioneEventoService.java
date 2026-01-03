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

    @Autowired
    private EmailService emailService; // Iniettiamo l'interfaccia

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

        // 5. Controllo Stato Organizzatore [SO] - TC_UC4_
        UtenteRegistrato organizzatore = utenteRepository.findById(emailOrganizzatore)
                .orElseThrow(() -> new IllegalArgumentException("Errato: Organizzatore non valido"));


        String indirizzoFinale;
        Double latFinale;
        Double lonFinale;

        // CASO A: L'utente ha mandato le COORDINATE (Click su mappa)
        if (eventoDTO.getLatitudine() != null && eventoDTO.getLongitudine() != null
                && (Math.abs(eventoDTO.getLatitudine()) > 0.001 || Math.abs(eventoDTO.getLongitudine()) > 0.001)) {

            latFinale = eventoDTO.getLatitudine();
            lonFinale = eventoDTO.getLongitudine();
            indirizzoFinale = mappeFacade.getIndirizzoDaCoordinate(latFinale, lonFinale);
        }

        // CASO B: L'utente ha mandato l'INDIRIZZO (Testo)
        else if (eventoDTO.getLuogo() != null) {

            if (eventoDTO.getLuogo().length() < 5) {
                throw new IllegalArgumentException("Errato: Indirizzo troppo breve o non valido");
            }

            indirizzoFinale = eventoDTO.getLuogo();

            // Usiamo il Facade per ottenere le coordinate (Geocoding)
            Double[] coordinate = mappeFacade.getCoordinate(indirizzoFinale);
            latFinale = coordinate[0];
            lonFinale = coordinate[1];
        }

        // CASO C: Non ha mandato niente
        else {
            throw new IllegalArgumentException("Errato: Devi inserire un luogo o selezionarlo sulla mappa.");
        }

        // Creazione Entity e settaggio stato iniziale (REQ6)
        EventoSportivo nuovoEvento = new EventoSportivo();
        nuovoEvento.setSport(eventoDTO.getSport());
        nuovoEvento.setDataOra(eventoDTO.getDataOra());
        nuovoEvento.setLuogo(indirizzoFinale);
        nuovoEvento.setNPartMax(eventoDTO.getNPartMax());
        nuovoEvento.setOrganizzatore(organizzatore);
        nuovoEvento.setLatitudine(latFinale);
        nuovoEvento.setLongitudine(lonFinale);
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

            emailService.inviaEmail(
                    utente.getEmail(),
                    "Evento Annullato",
                    "Ciao " + utente.getNome() + ", l'evento " + evento.getSport() + " è stato annullato."
            );
        }
    }
}
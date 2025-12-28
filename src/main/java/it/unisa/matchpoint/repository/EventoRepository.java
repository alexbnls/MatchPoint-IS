package it.unisa.matchpoint.repository;

import it.unisa.matchpoint.model.EventoSportivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<EventoSportivo, Integer> {
    // Per REQ_4: Filtrare gli eventi in base allo sport
    List<EventoSportivo> findBySportIgnoreCase(String sport);

    // Per visualizzare solo gli eventi creati da un determinato utente
    List<EventoSportivo> findByOrganizzatoreEmail(String email);

    // Per REQ_8: Trovare solo eventi in un determinato stato (es. "IN_ATTESA_DI_PARTECIPANTI")
    List<EventoSportivo> findByStato(Enum<?> stato);
}
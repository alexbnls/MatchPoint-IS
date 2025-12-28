package it.unisa.matchpoint.repository;

import it.unisa.matchpoint.model.Iscrizione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IscrizioneRepository extends JpaRepository<Iscrizione, Integer> {
    // Serve per il vincolo TCS "Utente già iscritto"
    boolean existsByEventoIdAndUtenteEmail(Integer idEvento, String emailUtente);

    // Serve per recuperare gli iscritti per le notifiche (Observer pattern)
    List<Iscrizione> findByEventoId(Integer idEvento);
}
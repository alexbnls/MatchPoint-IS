package it.unisa.matchpoint.repository;

import it.unisa.matchpoint.model.Feedback;
import it.unisa.matchpoint.model.UtenteRegistrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    // Serve per il vincolo TCS "Unicità Feedback"
    boolean existsByEventoIdAndValutatoreEmailAndValutatoEmail(Integer evento_id, String emailValutatore, String emailValutato);

    // Conta le valutazioni dell'utente
    long countByValutato(UtenteRegistrato valutato);
}
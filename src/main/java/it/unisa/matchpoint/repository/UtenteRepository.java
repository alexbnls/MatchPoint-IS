package it.unisa.matchpoint.repository;

import it.unisa.matchpoint.model.UtenteRegistrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtenteRepository extends JpaRepository<UtenteRegistrato, String> {
    // Serve per controllare duplicati mail nel UC_1
    boolean existsByEmail(String email);
}
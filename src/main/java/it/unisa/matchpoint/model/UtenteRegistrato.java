package it.unisa.matchpoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "UtenteRegistrato") // Nome entità definito nel System Design
@Data // Genera getter, setter e costruttori tramite Lombok
@NoArgsConstructor
@AllArgsConstructor
public class UtenteRegistrato {

    @Id // L'email è la Primary Key
    @Column(length = 320, nullable = false)
    // Regex validazione email da specifica Test Case [cite: 20]
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")
    private String email;

    @Column(nullable = false)
    // Regex validazione password da specifica Test Case [cite: 21]
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    private String password;

    @Column(nullable = false, length = 50) // Lunghezza da Dizionario Dati
    // Regex validazione nome da specifica Test Case [cite: 18]
    @Pattern(regexp = "^[A-Za-z\\s']{2,30}$")
    private String nome;

    @Column(nullable = false, length = 50) // Lunghezza da Dizionario Dati
    // Regex validazione cognome da specifica Test Case [cite: 19]
    @Pattern(regexp = "^[A-Za-z\\s']{2,30}$")
    private String cognome;

    @Column(name = "ubicazione_predefinita", length = 100)
    private String ubicazionePred; // Preferenza indicata nel RAD [cite: 159]

    // Rating con valori di default 0.0
    // Usiamo columnDefinition per forzare il database a creare un DECIMAL(2,1)
    @Column(name = "rating_affidabilita", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    private Double ratingAffidabilita = 0.0;

    @Column(name = "rating_abilita", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    private Double ratingAbilita = 0.0;

    @Column(name = "rating_sportivita", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    private Double ratingSportivita = 0.0;

    @Column(length = 20)
    private String ruolo = "utente"; // Ruolo di default
}
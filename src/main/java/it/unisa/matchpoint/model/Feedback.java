package it.unisa.matchpoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "Feedback", // Nome entità definito nel System Design
        uniqueConstraints = {
                // Implementazione vincolo TCS "Unicità Feedback"
                // Un valutatore non può valutare lo stesso utente due volte per lo stesso evento
                @UniqueConstraint(
                        name = "UniqueFeedbackPerUserAndEvent",
                        columnNames = {"id_evento", "valutatore", "valutato"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feedback") // Definito nel Dizionario Dati
    private Integer id;

    // --- PUNTEGGI ---
    // Mapping Decimal(1,1) SDD  e Validazione Range 1-5 TCS

    @Column(name = "punteggio_abilita", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    @NotNull
    @Min(1)
    @Max(5)
    private Double punteggioAbilita;

    @Column(name = "punteggio_aff", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    @NotNull
    @Min(1)
    @Max(5)
    private Double punteggioAffidabilita;

    @Column(name = "punteggio_sport", columnDefinition = "DECIMAL(2,1) DEFAULT 0.0")
    @NotNull
    @Min(1)
    @Max(5)
    private Double punteggioSportivita;

    // Impostato come NULLABLE perché definito "opzionale".
    @Column(name = "descrizione", length = 500)
    private String descrizione;

    // --- RELAZIONI (Foreign Keys) ---
    // Definite nel Dizionario Dati SDD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private EventoSportivo evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valutatore", referencedColumnName = "email", nullable = false)
    private UtenteRegistrato valutatore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valutato", referencedColumnName = "email", nullable = false)
    private UtenteRegistrato valutato;
}
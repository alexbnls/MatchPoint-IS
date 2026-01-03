package it.unisa.matchpoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Messaggio") // Definito nel Dizionario Dati
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Messaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_messaggio")
    private Integer id;

    // Nel SDD il tipo è "Text". In MySQL questo va mappato esplicitamente
    // o con @Lob, altrimenti JPA crea un Varchar(255).
    // Uso columnDefinition come richiesto per coerenza.
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String testo;

    @Column(name = "data_invio", nullable = false)
    @NotNull
    private LocalDateTime dataInvio;

    // --- RELAZIONI ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private EventoSportivo evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autore", referencedColumnName = "email", nullable = false)
    private UtenteRegistrato autore;
}
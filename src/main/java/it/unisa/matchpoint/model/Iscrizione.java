package it.unisa.matchpoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Iscrizione") // Definito nel Dizionario Dati
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_iscrizione")
    private Integer id;

    @Column(name = "data_iscrizione", nullable = false)
    @NotNull
    private LocalDateTime dataIscrizione;

    // Enum definito nel Dizionario Dati: "In attesa", "Confermato", "Annullato"
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoIscrizione stato;

    // --- RELAZIONI ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable = false)
    private EventoSportivo evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_utente", referencedColumnName = "email", nullable = false)
    private UtenteRegistrato utente;
}

enum StatoIscrizione {
    IN_ATTESA,
    CONFERMATO,
    ANNULLATO
}
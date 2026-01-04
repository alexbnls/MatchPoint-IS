package it.unisa.matchpoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Nota: In un'implementazione reale, questa classe estenderebbe 'SubjectEvento'
// come definito nel documento Design Pattern.
// Qui includiamo la logica direttamente per completezza dell'Entity.

@Entity
@Table(name = "EventoSportivo") // Definito nel Dizionario Dati
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoSportivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer id;

    // --- DATI PRINCIPALI ---

    @Column(nullable = false, length = 30) // SDD
    @Pattern(regexp = "^[a-zA-Z\\s]{3,30}$") // Validazione TCS
    private String sport;

    @Column(name = "data_ora", nullable = false)
    @FutureOrPresent // Validazione TCS (Data evento >= Data odierna)
    private LocalDateTime dataOra;

    // --- LUOGO & GEOLOCALIZZAZIONE ---

    @Column(nullable = false, length = 255)
    @Pattern(regexp = ".{5,150}", message = "Indirizzo non valido o troppo lungo")
    private String luogo;

    @Column(name = "latitudine", columnDefinition = "DECIMAL(9,6)")
    private Double latitudine;

    @Column(name = "longitudine", columnDefinition = "DECIMAL(9,6)")
    private Double longitudine;

    // --- PARTECIPANTI ---

    @Column(name = "n_part_max", nullable = false)
    @Min(2) // Validazione TCS
    private Integer nPartMax;

    @Column(name = "n_part_attuali", nullable = false)
    private Integer nPartAttuali = 0; // Default 0 da SDD

    // --- STATO & ESITO ---

    @Column(length = 50)
    private String risultato; // Nullable da SDD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoEvento stato;

    // --- RELAZIONI ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizzatore", referencedColumnName = "email", nullable = false)
    private UtenteRegistrato organizzatore; // FK definita in SDD

    // --- LOGICA DESIGN PATTERN (OBSERVER) ---

    /**
     * Override del setter di Lombok per implementare l'Observer Pattern.
     * "Quando lo stato cambia, invoca il metodo notificaOsservatori()."
     */
    public void setStato(StatoEvento nuovoStato) {
        this.stato = nuovoStato;

        // Logica Observer: se l'evento viene annullato, notifica gli iscritti.
        if (nuovoStato == StatoEvento.ANNULLATO) {
            notificaOsservatori();
        }
    }

    // Metodo stub per simulare la classe padre SubjectEvento
    private void notificaOsservatori() {
        // Implementazione delegata al Service o classe padre
        System.out.println("Notifica inviata agli osservatori per cambio stato evento: " + this.id);
    }
}


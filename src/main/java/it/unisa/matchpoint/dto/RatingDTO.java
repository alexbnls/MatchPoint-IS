package it.unisa.matchpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    // Usiamo BigDecimal per mappare correttamente il DECIMAL(2,1) del DB
    private Double abilita;
    private Double affidabilita;
    private Double sportivita;
    private String descrizione;
}
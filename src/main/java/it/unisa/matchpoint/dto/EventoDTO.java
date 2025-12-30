package it.unisa.matchpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoDTO {
    private String sport;
    private LocalDateTime dataOra;
    private String luogo;
    private Integer nPartMax;
    private Double latitudine;
    private Double longitudine;
}
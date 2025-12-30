package it.unisa.matchpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtenteDTO {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String confermaPassword;
    private String ubicazionePred;
}
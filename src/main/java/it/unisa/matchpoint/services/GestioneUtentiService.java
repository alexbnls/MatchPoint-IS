package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.UtenteDTO;
import it.unisa.matchpoint.model.UtenteRegistrato;
import it.unisa.matchpoint.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class GestioneUtentiService {

    @Autowired
    private UtenteRepository utenteRepository;

    @Transactional
    public UtenteRegistrato registraUtente(UtenteDTO datiUtente) {

        // 1. Controllo Formato Nome [FNO] - TC_UC1_1
        if (!datiUtente.getNome().matches("^[A-Za-z\\s']{2,30}$")) {
            throw new IllegalArgumentException("Errato: Formato nome non valido");
        }

        // 2. Controllo Formato Cognome [FCO] - TC_UC1_2
        if (!datiUtente.getCognome().matches("^[A-Za-z\\s']{2,30}$")) {
            throw new IllegalArgumentException("Errato: Formato cognome non valido");
        }

        // 3. Controllo Formato Email [FE] - TC_UC1_3
        if (!datiUtente.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$")) {
            throw new IllegalArgumentException("Errato: Formato email non valido");
        }

        // 4. Controllo Disponibilità Email [DE] - TC_UC1_4
        if (utenteRepository.existsByEmail(datiUtente.getEmail())) {
            throw new IllegalArgumentException("Errato: Email già in uso");
        }

        // 5. Controllo Validità Password [VP] - TC_UC1_5
        if (!datiUtente.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("Errato: Password debole");
        }

        // 6. Controllo Match Password [MP] - TC_UC1_6
        if (!datiUtente.getPassword().equals(datiUtente.getConfermaPassword())) {
            throw new IllegalArgumentException("Errato: Le password non coincidono");
        }

        // 7. Esito Corretto - TC_UC1_7
        UtenteRegistrato utente = new UtenteRegistrato();
        utente.setEmail(datiUtente.getEmail());
        utente.setNome(datiUtente.getNome());
        utente.setCognome(datiUtente.getCognome());
        String passwordCifrata = hashPassword(datiUtente.getPassword());
        utente.setPassword(passwordCifrata);
        utente.setRuolo("utente");
        utente.setUbicazionePred(datiUtente.getUbicazionePred());

        return utenteRepository.save(utente);
    }
    private String hashPassword(String passwordInChiaro) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(passwordInChiaro.getBytes(StandardCharsets.UTF_8));
            // Convertiamo i byte in una stringa leggibile
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore critico durante l'hashing della password", e);
        }
    }
    public UtenteRegistrato login(String email, String password) {
        // Recupera l'utente dal DB
        Optional<UtenteRegistrato> utente = utenteRepository.findById(email);

        // Verifica esistenza e corrispondenza password
        if (utente.isPresent()) {
            String passwordInseritaHash = hashPassword(password);

            if (utente.get().getPassword().equals(passwordInseritaHash)) {
                return utente.get();
            }
        }

        // Se l'utente non c'è o la password non coincide
        throw new IllegalArgumentException("Errore: Credenziali non valide");
    }
    }
package it.unisa.matchpoint.services;

import it.unisa.matchpoint.dto.NominatimResponseDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service // Carica all'avvio
public class MappeServiceFacadeImpl implements MappeServiceFacade {

    private final String BASE_URL = "https://nominatim.openstreetmap.org/search";
    private final String SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private final String REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    @Override
    public Double[] getCoordinate(String indirizzo) {

        if (indirizzo == null || indirizzo.length() < 5) {
            throw new IllegalArgumentException("Indirizzo non valido");
        }
        try {
            // 2. Preparazione della richiesta
            RestTemplate restTemplate = new RestTemplate();

            // OpenStreetMap richiede un Header "User-Agent" per identificare l'app,
            // altrimenti blocca la richiesta (errore 403).
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MatchPoint-StudentProject/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Costruiamo l'URL: search?q=indirizzo&format=json&limit=1
            String url = BASE_URL + "?q=" + indirizzo + "&format=json&limit=1";

            // 3. Esecuzione della chiamata GET
            ResponseEntity<NominatimResponseDTO[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResponseDTO[].class
            );

            // 4. Elaborazione risposta
            NominatimResponseDTO[] risultati = response.getBody();

            if (risultati != null && risultati.length > 0) {
                // OpenStreetMap restituisce le coordinate come Stringhe, dobbiamo convertirle
                Double lat = Double.parseDouble(risultati[0].getLat());
                Double lon = Double.parseDouble(risultati[0].getLon());

                // Restituisce [Latitudine, Longitudine]
                return new Double[]{lat, lon};
            } else {
                throw new IllegalArgumentException("Indirizzo non trovato: " + indirizzo);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante la geolocalizzazione: " + e.getMessage());
        }
    }

    @Override
    public String getIndirizzoDaCoordinate(Double lat, Double lon) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MatchPoint-StudentProject/1.0"); // Sempre necessario
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // URL: reverse?format=json&lat=...&lon=...
            String url = REVERSE_URL + "?format=json&lat=" + lat + "&lon=" + lon;

            // Chiamata GET
            ResponseEntity<NominatimResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResponseDTO.class
            );

            NominatimResponseDTO risultato = response.getBody();
            if (risultato != null && risultato.getDisplayName() != null) {
                return risultato.getDisplayName(); // Ritorna l'indirizzo testuale (es. "Via Roma, Salerno...")
            } else {
                return "Indirizzo sconosciuto";
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Errore reverse geocoding: " + e.getMessage());
        }
    }
}

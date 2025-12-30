package it.unisa.matchpoint.services; // O il package dove hai messo l'interfaccia

import org.springframework.stereotype.Service;

@Service // <--- Questo dice a Spring: "Caricami all'avvio!"
public class MappeServiceFacadeImpl implements MappeServiceFacade {

    @Override
    public Double[] getCoordinate(String indirizzo) {
        // SIMULAZIONE: In un progetto reale qui faresti una chiamata HTTP a Google Maps.
        // Per l'esame/test, restituiamo coordinate fisse o facciamo una logica semplice.

        if (indirizzo == null || indirizzo.length() < 5) {
            throw new IllegalArgumentException("Indirizzo non valido");
        }

        // Restituisce coordinate di esempio (es. Università di Salerno)
        // [Latitudine, Longitudine]
        return new Double[]{40.775, 14.789};
    }
}

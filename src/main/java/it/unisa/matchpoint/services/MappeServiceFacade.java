package it.unisa.matchpoint.services;

public interface MappeServiceFacade {

    // Dato un indirizzo, restituisce un array [latitudine, longitudine]
    // O lancia eccezione se l'indirizzo non esiste
    Double[] getCoordinate(String indirizzo);

    String getIndirizzoDaCoordinate(Double lat, Double lon);
}
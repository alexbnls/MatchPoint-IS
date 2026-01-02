package it.unisa.matchpoint.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponseDTO {
    private String lat;
    private String lon;
    @JsonProperty("display_name")
    private String displayName;
}
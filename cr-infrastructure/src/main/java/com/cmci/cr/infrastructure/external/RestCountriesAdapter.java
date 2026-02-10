package com.cmci.cr.infrastructure.external;

import com.cmci.cr.domain.port.CountryDataPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter qui récupère les données géographiques depuis l'API RestCountries
 * API gratuite, sans clé : https://restcountries.com
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestCountriesAdapter implements CountryDataPort {

    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,region,cca2";

    private final RestTemplate restTemplate;

    @Override
    public Map<String, List<CountryInfo>> getCountriesGroupedByRegion() {
        log.info("Fetching countries from RestCountries API...");

        var response = restTemplate.exchange(
                API_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RestCountryDto>>() {}
        );

        List<RestCountryDto> countries = response.getBody();
        if (countries == null || countries.isEmpty()) {
            throw new IllegalStateException("Aucune donnée reçue de l'API RestCountries");
        }

        log.info("Received {} countries from API", countries.size());

        return countries.stream()
                .filter(c -> c.getRegion() != null && !c.getRegion().isBlank()
                        && !"Antarctic".equals(c.getRegion()))
                .collect(Collectors.groupingBy(
                        RestCountryDto::getRegion,
                        TreeMap::new,
                        Collectors.mapping(
                                c -> new CountryInfo(c.getName().getCommon(), c.getCca2()),
                                Collectors.toList()
                        )
                ));
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RestCountryDto {
        private NameDto name;
        private String region;
        private String cca2;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class NameDto {
            private String common;
        }
    }
}

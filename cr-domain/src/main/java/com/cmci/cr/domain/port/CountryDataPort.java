package com.cmci.cr.domain.port;

import java.util.List;
import java.util.Map;

/**
 * Port pour récupérer les données géographiques (continents et pays)
 * depuis une source externe
 */
public interface CountryDataPort {

    /**
     * Récupère tous les pays groupés par continent/région
     * @return Map avec le nom du continent comme clé et la liste des pays comme valeur
     */
    Map<String, List<CountryInfo>> getCountriesGroupedByRegion();

    record CountryInfo(String name, String code) {}
}

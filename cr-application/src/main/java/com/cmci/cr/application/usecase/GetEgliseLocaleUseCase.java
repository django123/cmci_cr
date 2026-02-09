package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.EgliseLocaleResponse;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetEgliseLocaleUseCase {

    private final EgliseLocaleRepository egliseLocaleRepository;
    private final ZoneRepository zoneRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseLocaleResponse getById(UUID id) {
        EgliseLocale eglise = egliseLocaleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Église locale non trouvée avec l'ID: " + id));
        return mapToResponse(eglise);
    }

    public List<EgliseLocaleResponse> getAll() {
        return egliseLocaleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EgliseLocaleResponse> getByZoneId(UUID zoneId) {
        return egliseLocaleRepository.findByZoneId(zoneId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EgliseLocaleResponse mapToResponse(EgliseLocale eglise) {
        String zoneNom = zoneRepository.findById(eglise.getZoneId())
                .map(z -> z.getNom())
                .orElse(null);

        String pasteurNom = null;
        if (eglise.getPasteurId() != null) {
            pasteurNom = utilisateurRepository.findById(eglise.getPasteurId())
                    .map(u -> u.getNomComplet())
                    .orElse(null);
        }

        return EgliseLocaleResponse.builder()
                .id(eglise.getId())
                .nom(eglise.getNom())
                .zoneId(eglise.getZoneId())
                .zoneNom(zoneNom)
                .adresse(eglise.getAdresse())
                .pasteurId(eglise.getPasteurId())
                .pasteurNom(pasteurNom)
                .nombreEglisesMaison(egliseMaisonRepository.countByEgliseLocaleId(eglise.getId()))
                .createdAt(eglise.getCreatedAt())
                .updatedAt(eglise.getUpdatedAt())
                .build();
    }
}

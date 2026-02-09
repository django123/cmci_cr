package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.UpdateEgliseLocaleCommand;
import com.cmci.cr.application.dto.response.EgliseLocaleResponse;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class UpdateEgliseLocaleUseCase {

    private final EgliseLocaleRepository egliseLocaleRepository;
    private final ZoneRepository zoneRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseLocaleResponse execute(UpdateEgliseLocaleCommand command) {
        EgliseLocale existing = egliseLocaleRepository.findById(command.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Église locale non trouvée avec l'ID: " + command.getId()));

        EgliseLocale updated = existing;

        if (command.getNom() != null) {
            updated = updated.withNom(command.getNom());
        }
        if (command.getZoneId() != null) {
            zoneRepository.findById(command.getZoneId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Zone non trouvée avec l'ID: " + command.getZoneId()));
            updated = updated.withZoneId(command.getZoneId());
        }
        if (command.getAdresse() != null) {
            updated = updated.withAdresse(command.getAdresse());
        }
        if (command.getPasteurId() != null) {
            utilisateurRepository.findById(command.getPasteurId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Pasteur non trouvé avec l'ID: " + command.getPasteurId()));
            updated = updated.withPasteurId(command.getPasteurId());
        }

        updated = updated.withUpdatedAt(LocalDateTime.now());
        updated.validate();

        EgliseLocale saved = egliseLocaleRepository.save(updated);
        return mapToResponse(saved);
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

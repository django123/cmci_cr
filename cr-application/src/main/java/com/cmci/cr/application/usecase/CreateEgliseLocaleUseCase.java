package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateEgliseLocaleCommand;
import com.cmci.cr.application.dto.response.EgliseLocaleResponse;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateEgliseLocaleUseCase {

    private final EgliseLocaleRepository egliseLocaleRepository;
    private final ZoneRepository zoneRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseLocaleResponse execute(CreateEgliseLocaleCommand command) {
        Zone zone = zoneRepository.findById(command.getZoneId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Zone non trouvée avec l'ID: " + command.getZoneId()));

        String pasteurNom = null;
        if (command.getPasteurId() != null) {
            Utilisateur pasteur = utilisateurRepository.findById(command.getPasteurId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Pasteur non trouvé avec l'ID: " + command.getPasteurId()));
            pasteurNom = pasteur.getNomComplet();
        }

        EgliseLocale egliseLocale = EgliseLocale.builder()
                .id(UUID.randomUUID())
                .nom(command.getNom())
                .zoneId(command.getZoneId())
                .adresse(command.getAdresse())
                .pasteurId(command.getPasteurId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        egliseLocale.validate();

        EgliseLocale saved = egliseLocaleRepository.save(egliseLocale);
        return mapToResponse(saved, zone.getNom(), pasteurNom);
    }

    private EgliseLocaleResponse mapToResponse(EgliseLocale eglise, String zoneNom, String pasteurNom) {
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

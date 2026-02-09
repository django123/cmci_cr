package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.UpdateEgliseMaisonCommand;
import com.cmci.cr.application.dto.response.EgliseMaisonResponse;
import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class UpdateEgliseMaisonUseCase {

    private final EgliseMaisonRepository egliseMaisonRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseMaisonResponse execute(UpdateEgliseMaisonCommand command) {
        EgliseMaison existing = egliseMaisonRepository.findById(command.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Église de maison non trouvée avec l'ID: " + command.getId()));

        EgliseMaison updated = existing;

        if (command.getNom() != null) {
            updated = updated.withNom(command.getNom());
        }
        if (command.getEgliseLocaleId() != null) {
            egliseLocaleRepository.findById(command.getEgliseLocaleId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Église locale non trouvée avec l'ID: " + command.getEgliseLocaleId()));
            updated = updated.withEgliseLocaleId(command.getEgliseLocaleId());
        }
        if (command.getLeaderId() != null) {
            utilisateurRepository.findById(command.getLeaderId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Leader non trouvé avec l'ID: " + command.getLeaderId()));
            updated = updated.withLeaderId(command.getLeaderId());
        }
        if (command.getAdresse() != null) {
            updated = updated.withAdresse(command.getAdresse());
        }

        updated = updated.withUpdatedAt(LocalDateTime.now());
        updated.validate();

        EgliseMaison saved = egliseMaisonRepository.save(updated);
        return mapToResponse(saved);
    }

    private EgliseMaisonResponse mapToResponse(EgliseMaison eglise) {
        String egliseLocaleNom = egliseLocaleRepository.findById(eglise.getEgliseLocaleId())
                .map(el -> el.getNom())
                .orElse(null);

        String leaderNom = null;
        if (eglise.getLeaderId() != null) {
            leaderNom = utilisateurRepository.findById(eglise.getLeaderId())
                    .map(u -> u.getNomComplet())
                    .orElse(null);
        }

        long nombreFideles = utilisateurRepository.findByEgliseMaisonId(eglise.getId()).size();

        return EgliseMaisonResponse.builder()
                .id(eglise.getId())
                .nom(eglise.getNom())
                .egliseLocaleId(eglise.getEgliseLocaleId())
                .egliseLocaleNom(egliseLocaleNom)
                .leaderId(eglise.getLeaderId())
                .leaderNom(leaderNom)
                .adresse(eglise.getAdresse())
                .nombreFideles(nombreFideles)
                .createdAt(eglise.getCreatedAt())
                .updatedAt(eglise.getUpdatedAt())
                .build();
    }
}

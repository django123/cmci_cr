package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateEgliseMaisonCommand;
import com.cmci.cr.application.dto.response.EgliseMaisonResponse;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateEgliseMaisonUseCase {

    private final EgliseMaisonRepository egliseMaisonRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseMaisonResponse execute(CreateEgliseMaisonCommand command) {
        EgliseLocale egliseLocale = egliseLocaleRepository.findById(command.getEgliseLocaleId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Église locale non trouvée avec l'ID: " + command.getEgliseLocaleId()));

        String leaderNom = null;
        if (command.getLeaderId() != null) {
            Utilisateur leader = utilisateurRepository.findById(command.getLeaderId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Leader non trouvé avec l'ID: " + command.getLeaderId()));
            leaderNom = leader.getNomComplet();
        }

        EgliseMaison egliseMaison = EgliseMaison.builder()
                .id(UUID.randomUUID())
                .nom(command.getNom())
                .egliseLocaleId(command.getEgliseLocaleId())
                .leaderId(command.getLeaderId())
                .adresse(command.getAdresse())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        egliseMaison.validate();

        EgliseMaison saved = egliseMaisonRepository.save(egliseMaison);
        return mapToResponse(saved, egliseLocale.getNom(), leaderNom);
    }

    private EgliseMaisonResponse mapToResponse(EgliseMaison eglise, String egliseLocaleNom, String leaderNom) {
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

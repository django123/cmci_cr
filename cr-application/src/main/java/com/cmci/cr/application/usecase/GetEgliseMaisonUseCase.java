package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.EgliseMaisonResponse;
import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetEgliseMaisonUseCase {

    private final EgliseMaisonRepository egliseMaisonRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EgliseMaisonResponse getById(UUID id) {
        EgliseMaison eglise = egliseMaisonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Église de maison non trouvée avec l'ID: " + id));
        return mapToResponse(eglise);
    }

    public List<EgliseMaisonResponse> getAll() {
        return egliseMaisonRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EgliseMaisonResponse> getByEgliseLocaleId(UUID egliseLocaleId) {
        return egliseMaisonRepository.findByEgliseLocaleId(egliseLocaleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

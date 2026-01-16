package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Consulter les utilisateurs
 */
@RequiredArgsConstructor
public class GetUtilisateurUseCase {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupère un utilisateur par son ID
     */
    public UtilisateurResponse getById(UUID id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur non trouvé avec l'ID: " + id
                ));

        // Récupérer le FD si présent
        Utilisateur fd = null;
        if (utilisateur.getFdId() != null) {
            fd = utilisateurRepository.findById(utilisateur.getFdId()).orElse(null);
        }

        return mapToResponse(utilisateur, fd);
    }

    /**
     * Récupère un utilisateur par son email
     */
    public UtilisateurResponse getByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur non trouvé avec l'email: " + email
                ));

        // Récupérer le FD si présent
        Utilisateur fd = null;
        if (utilisateur.getFdId() != null) {
            fd = utilisateurRepository.findById(utilisateur.getFdId()).orElse(null);
        }

        return mapToResponse(utilisateur, fd);
    }

    /**
     * Récupère tous les disciples d'un FD
     */
    public List<UtilisateurResponse> getDisciplesByFdId(UUID fdId) {
        // Vérifier que le FD existe
        Utilisateur fd = utilisateurRepository.findById(fdId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "FD non trouvé avec l'ID: " + fdId
                ));

        return utilisateurRepository.findByFdId(fdId)
                .stream()
                .map(disciple -> mapToResponse(disciple, fd))
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les utilisateurs d'une église de maison
     */
    public List<UtilisateurResponse> getByEgliseMaisonId(UUID egliseMaisonId) {
        return utilisateurRepository.findByEgliseMaisonId(egliseMaisonId)
                .stream()
                .map(utilisateur -> {
                    Utilisateur fd = null;
                    if (utilisateur.getFdId() != null) {
                        fd = utilisateurRepository.findById(utilisateur.getFdId()).orElse(null);
                    }
                    return mapToResponse(utilisateur, fd);
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les utilisateurs avec un rôle spécifique
     */
    public List<UtilisateurResponse> getByRole(String roleStr) {
        Role role = Role.valueOf(roleStr);
        return utilisateurRepository.findByRole(role)
                .stream()
                .map(utilisateur -> {
                    Utilisateur fd = null;
                    if (utilisateur.getFdId() != null) {
                        fd = utilisateurRepository.findById(utilisateur.getFdId()).orElse(null);
                    }
                    return mapToResponse(utilisateur, fd);
                })
                .collect(Collectors.toList());
    }

    /**
     * Mappe un Utilisateur vers UtilisateurResponse
     */
    private UtilisateurResponse mapToResponse(Utilisateur utilisateur, Utilisateur fd) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .nomComplet(utilisateur.getNomComplet())
                .role(utilisateur.getRole().name())
                .egliseMaisonId(utilisateur.getEgliseMaisonId())
                .fdId(utilisateur.getFdId())
                .fdNom(fd != null ? fd.getNomComplet() : null)
                .avatarUrl(utilisateur.getAvatarUrl())
                .telephone(utilisateur.getTelephone())
                .dateNaissance(utilisateur.getDateNaissance())
                .dateBapteme(utilisateur.getDateBapteme())
                .statut(utilisateur.getStatut().name())
                .createdAt(utilisateur.getCreatedAt())
                .updatedAt(utilisateur.getUpdatedAt())
                .build();
    }
}

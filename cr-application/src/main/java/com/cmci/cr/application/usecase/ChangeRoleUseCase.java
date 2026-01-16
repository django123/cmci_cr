package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.ChangeRoleCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Use Case: Changer le rôle d'un utilisateur
 */
@RequiredArgsConstructor
public class ChangeRoleUseCase {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Exécute le use case de changement de rôle
     *
     * @param command Commande de changement de rôle
     * @return L'utilisateur avec son nouveau rôle
     * @throws IllegalArgumentException si l'utilisateur n'existe pas ou le rôle est invalide
     */
    public UtilisateurResponse execute(ChangeRoleCommand command) {
        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(command.getUtilisateurId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur non trouvé avec l'ID: " + command.getUtilisateurId()
                ));

        // Valider et convertir le nouveau rôle
        Role newRole;
        try {
            newRole = Role.valueOf(command.getNewRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Rôle invalide: " + command.getNewRole() +
                    ". Rôles valides: FIDELE, FD, LEADER, PASTEUR, ADMIN"
            );
        }

        // Vérifier si le rôle a changé
        if (utilisateur.getRole() == newRole) {
            throw new IllegalArgumentException(
                    "L'utilisateur a déjà le rôle: " + newRole
            );
        }

        // Changer le rôle
        Utilisateur updatedUser = utilisateur
                .withRole(newRole)
                .withUpdatedAt(LocalDateTime.now());

        // Sauvegarder
        Utilisateur saved = utilisateurRepository.save(updatedUser);

        // Récupérer le FD si présent
        Utilisateur fd = null;
        if (saved.getFdId() != null) {
            fd = utilisateurRepository.findById(saved.getFdId()).orElse(null);
        }

        // Mapper vers le DTO de réponse
        return mapToResponse(saved, fd);
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

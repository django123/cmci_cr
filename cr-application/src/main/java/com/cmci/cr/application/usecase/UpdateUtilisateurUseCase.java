package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.UpdateUtilisateurCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Use Case: Mettre à jour un utilisateur existant
 */
@RequiredArgsConstructor
public class UpdateUtilisateurUseCase {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Exécute le use case de mise à jour d'utilisateur
     *
     * @param command Commande de mise à jour
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    public UtilisateurResponse execute(UpdateUtilisateurCommand command) {
        // Récupérer l'utilisateur existant
        Utilisateur existingUser = utilisateurRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur non trouvé avec l'ID: " + command.getId()
                ));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (command.getEmail() != null && !command.getEmail().equals(existingUser.getEmail())) {
            if (utilisateurRepository.existsByEmail(command.getEmail())) {
                throw new IllegalArgumentException(
                        "Un utilisateur avec cet email existe déjà: " + command.getEmail()
                );
            }
        }

        // Créer l'utilisateur mis à jour
        Utilisateur updatedUser = existingUser
                .withEmail(command.getEmail() != null ? command.getEmail() : existingUser.getEmail())
                .withNom(command.getNom() != null ? command.getNom() : existingUser.getNom())
                .withPrenom(command.getPrenom() != null ? command.getPrenom() : existingUser.getPrenom())
                .withEgliseMaisonId(command.getEgliseMaisonId() != null ? command.getEgliseMaisonId() : existingUser.getEgliseMaisonId())
                .withAvatarUrl(command.getAvatarUrl() != null ? command.getAvatarUrl() : existingUser.getAvatarUrl())
                .withTelephone(command.getTelephone() != null ? command.getTelephone() : existingUser.getTelephone())
                .withDateNaissance(command.getDateNaissance() != null ? command.getDateNaissance() : existingUser.getDateNaissance())
                .withDateBapteme(command.getDateBapteme() != null ? command.getDateBapteme() : existingUser.getDateBapteme())
                .withUpdatedAt(LocalDateTime.now());

        // Valider
        updatedUser.validate();

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

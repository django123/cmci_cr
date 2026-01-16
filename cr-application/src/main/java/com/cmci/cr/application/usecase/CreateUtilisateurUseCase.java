package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateUtilisateurCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case: Créer un nouvel utilisateur
 */
@RequiredArgsConstructor
public class CreateUtilisateurUseCase {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Exécute le use case de création d'utilisateur
     *
     * @param command Commande de création
     * @return L'utilisateur créé
     * @throws IllegalArgumentException si l'email existe déjà
     */
    public UtilisateurResponse execute(CreateUtilisateurCommand command) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException(
                    "Un utilisateur avec cet email existe déjà: " + command.getEmail()
            );
        }

        // Créer le nouvel utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(command.getEmail())
                .nom(command.getNom())
                .prenom(command.getPrenom())
                .role(Role.valueOf(command.getRole()))
                .egliseMaisonId(command.getEgliseMaisonId())
                .fdId(command.getFdId())
                .avatarUrl(command.getAvatarUrl())
                .telephone(command.getTelephone())
                .dateNaissance(command.getDateNaissance())
                .dateBapteme(command.getDateBapteme())
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Valider le domaine
        utilisateur.validate();

        // Sauvegarder
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        // Mapper vers le DTO de réponse
        return mapToResponse(saved, null);
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

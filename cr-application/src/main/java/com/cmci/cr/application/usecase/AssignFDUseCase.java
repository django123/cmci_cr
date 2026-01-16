package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.AssignFDCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Use Case: Assigner un FD à un disciple
 */
@RequiredArgsConstructor
public class AssignFDUseCase {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Exécute le use case d'assignation de FD
     *
     * @param command Commande d'assignation
     * @return Le disciple avec son FD assigné
     * @throws IllegalArgumentException si le disciple ou le FD n'existe pas
     */
    public UtilisateurResponse execute(AssignFDCommand command) {
        // Récupérer le disciple
        Utilisateur disciple = utilisateurRepository.findById(command.getDiscipleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Disciple non trouvé avec l'ID: " + command.getDiscipleId()
                ));

        // Vérifier que le FD existe et a le bon rôle si on assigne (non null)
        Utilisateur fd = null;
        if (command.getFdId() != null) {
            fd = utilisateurRepository.findById(command.getFdId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "FD non trouvé avec l'ID: " + command.getFdId()
                    ));

            // Vérifier que le FD a le rôle approprié
            if (fd.getRole() != Role.FD && fd.getRole() != Role.LEADER &&
                fd.getRole() != Role.PASTEUR && fd.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException(
                        "L'utilisateur " + fd.getNomComplet() + " n'a pas le rôle de FD"
                );
            }
        }

        // Assigner le FD au disciple
        Utilisateur updatedDisciple = disciple
                .withFdId(command.getFdId())
                .withUpdatedAt(LocalDateTime.now());

        // Sauvegarder
        Utilisateur saved = utilisateurRepository.save(updatedDisciple);

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

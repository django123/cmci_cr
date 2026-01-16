package com.cmci.cr.controller;

import com.cmci.cr.application.dto.command.AddCommentaireCommand;
import com.cmci.cr.application.dto.response.CommentaireResponse;
import com.cmci.cr.application.usecase.AddCommentaireUseCase;
import com.cmci.cr.application.usecase.GetCommentairesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des Commentaires sur les CR
 */
@RestController
@RequestMapping("/v1/commentaires")
@Tag(name = "Commentaires", description = "API de gestion des commentaires sur les comptes rendus")
public class CommentaireController {

    private final AddCommentaireUseCase addCommentaireUseCase;
    private final GetCommentairesUseCase getCommentairesUseCase;

    public CommentaireController(AddCommentaireUseCase addCommentaireUseCase,
                                 GetCommentairesUseCase getCommentairesUseCase) {
        this.addCommentaireUseCase = addCommentaireUseCase;
        this.getCommentairesUseCase = getCommentairesUseCase;
    }

    @PostMapping
    @Operation(summary = "Ajouter un commentaire",
               description = "Ajoute un commentaire sur un compte rendu")
    public ResponseEntity<CommentaireResponse> addCommentaire(
            @Valid @RequestBody AddCommentaireCommand command) {
        CommentaireResponse response = addCommentaireUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/compte-rendu/{compteRenduId}")
    @Operation(summary = "Récupérer les commentaires d'un CR",
               description = "Récupère tous les commentaires associés à un compte rendu")
    public ResponseEntity<List<CommentaireResponse>> getCommentairesByCompteRendu(
            @PathVariable UUID compteRenduId) {
        List<CommentaireResponse> responses = getCommentairesUseCase.getByCompteRenduId(compteRenduId);
        return ResponseEntity.ok(responses);
    }
}

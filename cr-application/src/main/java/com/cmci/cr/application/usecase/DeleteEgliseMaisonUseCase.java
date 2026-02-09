package com.cmci.cr.application.usecase;

import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class DeleteEgliseMaisonUseCase {

    private final EgliseMaisonRepository egliseMaisonRepository;
    private final UtilisateurRepository utilisateurRepository;

    public void execute(UUID id) {
        egliseMaisonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Église de maison non trouvée avec l'ID: " + id));

        long fidelesCount = utilisateurRepository.findByEgliseMaisonId(id).size();
        if (fidelesCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer l'église de maison: " + fidelesCount
                            + " fidèle(s) y sont rattaché(s)");
        }

        egliseMaisonRepository.deleteById(id);
    }
}

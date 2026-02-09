package com.cmci.cr.application.usecase;

import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class DeleteEgliseLocaleUseCase {

    private final EgliseLocaleRepository egliseLocaleRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;

    public void execute(UUID id) {
        egliseLocaleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Église locale non trouvée avec l'ID: " + id));

        long eglisesMaisonCount = egliseMaisonRepository.countByEgliseLocaleId(id);
        if (eglisesMaisonCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer l'église locale: " + eglisesMaisonCount
                            + " église(s) de maison y sont rattachée(s)");
        }

        egliseLocaleRepository.deleteById(id);
    }
}

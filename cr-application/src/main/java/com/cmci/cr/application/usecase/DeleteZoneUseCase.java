package com.cmci.cr.application.usecase;

import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class DeleteZoneUseCase {

    private final ZoneRepository zoneRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;

    public void execute(UUID id) {
        zoneRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Zone non trouvée avec l'ID: " + id));

        long eglisesCount = egliseLocaleRepository.countByZoneId(id);
        if (eglisesCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer la zone: " + eglisesCount + " église(s) locale(s) y sont rattachée(s)");
        }

        zoneRepository.deleteById(id);
    }
}

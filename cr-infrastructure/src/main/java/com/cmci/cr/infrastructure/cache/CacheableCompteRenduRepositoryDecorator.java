package com.cmci.cr.infrastructure.cache;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Décorateur qui ajoute le caching au repository CompteRendu
 */
@Component("cachedCompteRenduRepository")
@RequiredArgsConstructor
public class CacheableCompteRenduRepositoryDecorator implements CompteRenduRepository {

    private final CompteRenduRepository delegate;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COMPTES_RENDUS, key = "#result.id"),
            @CacheEvict(value = CacheNames.COMPTES_RENDUS,
                       key = "'user:' + #result.utilisateurId + ':date:' + #result.date"),
            @CacheEvict(value = CacheNames.STATISTIQUES, allEntries = true)
    })
    public CompteRendu save(CompteRendu compteRendu) {
        return delegate.save(compteRendu);
    }

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS, key = "#id", unless = "#result == null")
    public Optional<CompteRendu> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS,
               key = "'user:' + #utilisateurId + ':date:' + #date",
               unless = "#result == null")
    public Optional<CompteRendu> findByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date) {
        return delegate.findByUtilisateurIdAndDate(utilisateurId, date);
    }

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS,
               key = "'user:' + #utilisateurId + ':all'")
    public List<CompteRendu> findByUtilisateurId(UUID utilisateurId) {
        return delegate.findByUtilisateurId(utilisateurId);
    }

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS,
               key = "'user:' + #utilisateurId + ':period:' + #startDate + ':' + #endDate")
    public List<CompteRendu> findByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate) {
        return delegate.findByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);
    }

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS,
               key = "'user:' + #utilisateurId + ':unseen'")
    public List<CompteRendu> findByUtilisateurIdAndVuParFdFalse(UUID utilisateurId) {
        return delegate.findByUtilisateurIdAndVuParFdFalse(utilisateurId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COMPTES_RENDUS, key = "#id"),
            @CacheEvict(value = CacheNames.STATISTIQUES, allEntries = true)
    })
    public void deleteById(UUID id) {
        delegate.deleteById(id);
    }

    @Override
    public boolean existsByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date) {
        return delegate.existsByUtilisateurIdAndDate(utilisateurId, date);
    }

    @Override
    @Cacheable(value = CacheNames.STATISTIQUES,
               key = "'count:user:' + #utilisateurId + ':' + #startDate + ':' + #endDate")
    public long countByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate) {
        return delegate.countByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);
    }
}

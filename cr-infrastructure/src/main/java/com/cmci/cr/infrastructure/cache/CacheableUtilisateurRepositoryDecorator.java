package com.cmci.cr.infrastructure.cache;

import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Décorateur qui ajoute le caching au repository Utilisateur
 */
@Component("cachedUtilisateurRepository")
@RequiredArgsConstructor
public class CacheableUtilisateurRepositoryDecorator implements UtilisateurRepository {

    private final UtilisateurRepository delegate;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.UTILISATEURS, key = "#result.id"),
            @CacheEvict(value = CacheNames.UTILISATEURS, key = "'email:' + #result.email")
    })
    public Utilisateur save(Utilisateur utilisateur) {
        return delegate.save(utilisateur);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS, key = "#id", unless = "#result == null")
    public Optional<Utilisateur> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'email:' + #email",
               unless = "#result == null")
    public Optional<Utilisateur> findByEmail(String email) {
        return delegate.findByEmail(email);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'eglise:' + #egliseMaisonId")
    public List<Utilisateur> findByEgliseMaisonId(UUID egliseMaisonId) {
        return delegate.findByEgliseMaisonId(egliseMaisonId);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'fd:' + #fdId + ':disciples'")
    public List<Utilisateur> findByFdId(UUID fdId) {
        return delegate.findByFdId(fdId);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'role:' + #role.name()")
    public List<Utilisateur> findByRole(Role role) {
        return delegate.findByRole(role);
    }

    @Override
    public boolean existsByEmail(String email) {
        return delegate.existsByEmail(email);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.UTILISATEURS, key = "#id"),
            @CacheEvict(value = CacheNames.UTILISATEURS, allEntries = true)
    })
    public void deleteById(UUID id) {
        delegate.deleteById(id);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'fd:' + #fdId + ':count'")
    public long countByFdId(UUID fdId) {
        return delegate.countByFdId(fdId);
    }

    @Override
    @Cacheable(value = CacheNames.UTILISATEURS,
               key = "'eglises:' + #egliseMaisonIds.hashCode()")
    public List<Utilisateur> findByEgliseMaisonIdIn(List<UUID> egliseMaisonIds) {
        return delegate.findByEgliseMaisonIdIn(egliseMaisonIds);
    }
}

package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.CompteRenduJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour CompteRendu
 */
@Repository
public interface CompteRenduJpaRepository extends JpaRepository<CompteRenduJpaEntity, UUID> {

    /**
     * Trouve un CR par utilisateur et date
     */
    Optional<CompteRenduJpaEntity> findByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date);

    /**
     * Trouve tous les CR d'un utilisateur
     */
    List<CompteRenduJpaEntity> findByUtilisateurIdOrderByDateDesc(UUID utilisateurId);

    /**
     * Trouve les CR d'un utilisateur entre deux dates
     */
    @Query("SELECT cr FROM CompteRenduJpaEntity cr " +
           "WHERE cr.utilisateurId = :utilisateurId " +
           "AND cr.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.date DESC")
    List<CompteRenduJpaEntity> findByUtilisateurIdAndDateBetween(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Trouve les CR non vus d'un utilisateur
     */
    List<CompteRenduJpaEntity> findByUtilisateurIdAndVuParFdFalseOrderByDateDesc(UUID utilisateurId);

    /**
     * Vérifie si un CR existe pour une date donnée
     */
    boolean existsByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date);

    /**
     * Compte le nombre de CR d'un utilisateur sur une période
     */
    @Query("SELECT COUNT(cr) FROM CompteRenduJpaEntity cr " +
           "WHERE cr.utilisateurId = :utilisateurId " +
           "AND cr.date BETWEEN :startDate AND :endDate")
    long countByUtilisateurIdAndDateBetween(
            @Param("utilisateurId") UUID utilisateurId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Trouve les CR de plusieurs utilisateurs entre deux dates
     */
    @Query("SELECT cr FROM CompteRenduJpaEntity cr " +
           "WHERE cr.utilisateurId IN :utilisateurIds " +
           "AND cr.date BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.date DESC")
    List<CompteRenduJpaEntity> findByUtilisateurIdInAndDateBetween(
            @Param("utilisateurIds") List<UUID> utilisateurIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

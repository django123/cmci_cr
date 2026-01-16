package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import com.cmci.cr.infrastructure.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour CompteRenduRepositoryAdapter
 */
@Transactional
class CompteRenduRepositoryAdapterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompteRenduRepository compteRenduRepository;

    private UUID utilisateurId;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        utilisateurId = UUID.randomUUID();
        testDate = LocalDate.now();
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un compte rendu")
    void shouldSaveAndRetrieveCompteRendu() {
        // Given
        CompteRendu compteRendu = createTestCompteRendu();

        // When
        CompteRendu saved = compteRenduRepository.save(compteRendu);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUtilisateurId()).isEqualTo(utilisateurId);
        assertThat(saved.getDate()).isEqualTo(testDate);

        // Verify retrieval
        Optional<CompteRendu> retrieved = compteRenduRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getRdqd()).isEqualTo(RDQD.of(5, 7));
    }

    @Test
    @DisplayName("Devrait trouver un compte rendu par utilisateur et date")
    void shouldFindByUtilisateurIdAndDate() {
        // Given
        CompteRendu compteRendu = createTestCompteRendu();
        compteRenduRepository.save(compteRendu);

        // When
        Optional<CompteRendu> found = compteRenduRepository
                .findByUtilisateurIdAndDate(utilisateurId, testDate);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUtilisateurId()).isEqualTo(utilisateurId);
        assertThat(found.get().getDate()).isEqualTo(testDate);
    }

    @Test
    @DisplayName("Devrait trouver tous les comptes rendus d'un utilisateur")
    void shouldFindByUtilisateurId() {
        // Given
        CompteRendu cr1 = createTestCompteRendu();
        CompteRendu cr2 = createTestCompteRendu();
        cr2.setDate(testDate.minusDays(1));

        compteRenduRepository.save(cr1);
        compteRenduRepository.save(cr2);

        // When
        List<CompteRendu> found = compteRenduRepository.findByUtilisateurId(utilisateurId);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(cr -> cr.getUtilisateurId().equals(utilisateurId));
    }

    @Test
    @DisplayName("Devrait trouver les comptes rendus entre deux dates")
    void shouldFindByUtilisateurIdAndDateBetween() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;

        CompteRendu cr1 = createTestCompteRendu();
        cr1.setDate(testDate.minusDays(3));
        CompteRendu cr2 = createTestCompteRendu();
        cr2.setDate(testDate.minusDays(10)); // Hors période

        compteRenduRepository.save(cr1);
        compteRenduRepository.save(cr2);

        // When
        List<CompteRendu> found = compteRenduRepository
                .findByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDate()).isBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Devrait vérifier l'existence d'un compte rendu")
    void shouldCheckExistence() {
        // Given
        CompteRendu compteRendu = createTestCompteRendu();
        compteRenduRepository.save(compteRendu);

        // When
        boolean exists = compteRenduRepository
                .existsByUtilisateurIdAndDate(utilisateurId, testDate);
        boolean notExists = compteRenduRepository
                .existsByUtilisateurIdAndDate(utilisateurId, testDate.plusDays(1));

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Devrait compter les comptes rendus sur une période")
    void shouldCountByUtilisateurIdAndDateBetween() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;

        for (int i = 0; i < 5; i++) {
            CompteRendu cr = createTestCompteRendu();
            cr.setDate(testDate.minusDays(i));
            compteRenduRepository.save(cr);
        }

        // When
        long count = compteRenduRepository
                .countByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("Devrait supprimer un compte rendu")
    void shouldDeleteCompteRendu() {
        // Given
        CompteRendu compteRendu = createTestCompteRendu();
        CompteRendu saved = compteRenduRepository.save(compteRendu);

        // When
        compteRenduRepository.deleteById(saved.getId());

        // Then
        Optional<CompteRendu> deleted = compteRenduRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Devrait trouver les comptes rendus non vus")
    void shouldFindUnseenCompteRendus() {
        // Given
        CompteRendu crVu = createTestCompteRendu();
        crVu.setVuParFd(true);

        CompteRendu crNonVu = createTestCompteRendu();
        crNonVu.setDate(testDate.minusDays(1));
        crNonVu.setVuParFd(false);

        compteRenduRepository.save(crVu);
        compteRenduRepository.save(crNonVu);

        // When
        List<CompteRendu> unseen = compteRenduRepository
                .findByUtilisateurIdAndVuParFdFalse(utilisateurId);

        // Then
        assertThat(unseen).hasSize(1);
        assertThat(unseen.get(0).isVuParFd()).isFalse();
    }

    private CompteRendu createTestCompteRendu() {
        return CompteRendu.builder()
                .utilisateurId(utilisateurId)
                .date(testDate)
                .rdqd(RDQD.of(5, 7))
                .priereSeule(Duration.ofMinutes(30))
                .priereCouple(Duration.ofMinutes(15))
                .priereAvecEnfants(Duration.ofMinutes(10))
                .tempsEtudeParole(Duration.ofMinutes(45))
                .nombreContactsUtiles(3)
                .invitationsCulte(2)
                .offrande(BigDecimal.valueOf(5000))
                .evangelisations(1)
                .commentaire("Test commentaire")
                .statut(StatutCR.BROUILLON)
                .vuParFd(false)
                .build();
    }
}

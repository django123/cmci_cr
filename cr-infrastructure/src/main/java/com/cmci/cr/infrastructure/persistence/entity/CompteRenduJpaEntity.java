package com.cmci.cr.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour la table compte_rendu
 */
@Entity
@Table(name = "compte_rendu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"utilisateur_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteRenduJpaEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "rdqd", nullable = false, length = 10)
    private String rdqd;

    @Column(name = "priere_seule", nullable = false, length = 20)
    private String priereSeule;

    @Column(name = "lecture_biblique", nullable = false)
    private Integer lectureBiblique;

    @Column(name = "livre_biblique", length = 50)
    private String livreBiblique;

    @Column(name = "litterature_pages")
    private Integer litteraturePages;

    @Column(name = "litterature_total")
    private Integer litteratureTotal;

    @Column(name = "litterature_titre", length = 200)
    private String litteratureTitre;

    @Column(name = "priere_autres")
    @Builder.Default
    private Integer priereAutres = 0;

    @Column(name = "confession")
    @Builder.Default
    private Boolean confession = false;

    @Column(name = "jeune")
    @Builder.Default
    private Boolean jeune = false;

    @Column(name = "type_jeune", length = 50)
    private String typeJeune;

    @Column(name = "evangelisation")
    @Builder.Default
    private Integer evangelisation = 0;

    @Column(name = "offrande")
    @Builder.Default
    private Boolean offrande = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "statut", nullable = false, columnDefinition = "statut_cr_enum")
    private StatutCREnum statut;

    @Column(name = "vu_par_fd")
    @Builder.Default
    private Boolean vuParFd = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum StatutCREnum {
        BROUILLON,
        SOUMIS,
        VALIDE
    }
}

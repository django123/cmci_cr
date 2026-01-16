package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.Commentaire;
import com.cmci.cr.infrastructure.persistence.entity.CommentaireJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Commentaire (domain) et CommentaireJpaEntity (infrastructure)
 */
@Component
public class CommentaireMapper {

    public CommentaireJpaEntity toJpaEntity(Commentaire domain) {
        if (domain == null) {
            return null;
        }

        return CommentaireJpaEntity.builder()
                .id(domain.getId())
                .compteRenduId(domain.getCompteRenduId())
                .auteurId(domain.getAuteurId())
                .contenu(domain.getContenu())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public Commentaire toDomain(CommentaireJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return Commentaire.builder()
                .id(jpa.getId())
                .compteRenduId(jpa.getCompteRenduId())
                .auteurId(jpa.getAuteurId())
                .contenu(jpa.getContenu())
                .createdAt(jpa.getCreatedAt())
                .build();
    }
}

-- V1__init_schema.sql
-- Initialisation du schéma de base de données CMCI CR

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enum pour les rôles
CREATE TYPE role_enum AS ENUM ('FIDELE', 'FD', 'LEADER', 'PASTEUR', 'ADMIN');

-- Enum pour les statuts utilisateur
CREATE TYPE statut_utilisateur_enum AS ENUM ('ACTIF', 'INACTIF', 'SUSPENDU');

-- Enum pour les statuts CR
CREATE TYPE statut_cr_enum AS ENUM ('BROUILLON', 'SOUMIS', 'VALIDE');

-- Table des régions
CREATE TABLE region (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des zones
CREATE TABLE zone (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(100) NOT NULL,
    region_id UUID NOT NULL REFERENCES region(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des églises locales
CREATE TABLE eglise_locale (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(200) NOT NULL,
    zone_id UUID NOT NULL REFERENCES zone(id),
    adresse TEXT,
    pasteur_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des églises de maison
CREATE TABLE eglise_maison (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(200) NOT NULL,
    eglise_locale_id UUID NOT NULL REFERENCES eglise_locale(id),
    leader_id UUID,
    adresse TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des utilisateurs
CREATE TABLE utilisateur (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role role_enum NOT NULL DEFAULT 'FIDELE',
    eglise_maison_id UUID REFERENCES eglise_maison(id),
    fd_id UUID REFERENCES utilisateur(id),
    avatar_url VARCHAR(500),
    telephone VARCHAR(20),
    date_naissance DATE,
    date_bapteme DATE,
    statut statut_utilisateur_enum NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ajout des FK manquantes
ALTER TABLE eglise_locale ADD CONSTRAINT fk_pasteur
    FOREIGN KEY (pasteur_id) REFERENCES utilisateur(id);
ALTER TABLE eglise_maison ADD CONSTRAINT fk_leader
    FOREIGN KEY (leader_id) REFERENCES utilisateur(id);

-- Table des comptes rendus
CREATE TABLE compte_rendu (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    utilisateur_id UUID NOT NULL REFERENCES utilisateur(id),
    date DATE NOT NULL,
    rdqd VARCHAR(10) NOT NULL,
    priere_seule INTERVAL NOT NULL,
    lecture_biblique INTEGER NOT NULL CHECK (lecture_biblique >= 0),
    livre_biblique VARCHAR(50),
    litterature_pages INTEGER CHECK (litterature_pages >= 0),
    litterature_total INTEGER CHECK (litterature_total >= 0),
    litterature_titre VARCHAR(200),
    priere_autres INTEGER DEFAULT 0 CHECK (priere_autres >= 0),
    confession BOOLEAN DEFAULT FALSE,
    jeune BOOLEAN DEFAULT FALSE,
    type_jeune VARCHAR(50),
    evangelisation INTEGER DEFAULT 0 CHECK (evangelisation >= 0),
    offrande BOOLEAN DEFAULT FALSE,
    notes TEXT,
    statut statut_cr_enum NOT NULL DEFAULT 'SOUMIS',
    vu_par_fd BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (utilisateur_id, date)
);

-- Table des commentaires
CREATE TABLE commentaire_cr (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    compte_rendu_id UUID NOT NULL REFERENCES compte_rendu(id) ON DELETE CASCADE,
    auteur_id UUID NOT NULL REFERENCES utilisateur(id),
    contenu TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour les performances
CREATE INDEX idx_cr_utilisateur_date ON compte_rendu(utilisateur_id, date DESC);
CREATE INDEX idx_cr_date ON compte_rendu(date);
CREATE INDEX idx_utilisateur_fd ON utilisateur(fd_id);
CREATE INDEX idx_utilisateur_eglise ON utilisateur(eglise_maison_id);
CREATE INDEX idx_utilisateur_role ON utilisateur(role);
CREATE INDEX idx_cr_statut ON compte_rendu(statut);
CREATE INDEX idx_cr_non_vus ON compte_rendu(utilisateur_id, date) WHERE vu_par_fd = FALSE;

-- Trigger pour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_utilisateur_updated_at BEFORE UPDATE ON utilisateur
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_compte_rendu_updated_at BEFORE UPDATE ON compte_rendu
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Données initiales pour le développement
INSERT INTO region (nom, code) VALUES ('Afrique', 'AFR');
INSERT INTO zone (nom, region_id) VALUES ('RDC', (SELECT id FROM region WHERE code = 'AFR'));
INSERT INTO eglise_locale (nom, zone_id) VALUES
    ('Église CMCI Kinshasa', (SELECT id FROM zone WHERE nom = 'RDC'));

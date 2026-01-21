-- V2__seed_data.sql
-- Insertion des données de test pour l'application CMCI CR

-- =====================================================
-- REGIONS ET ZONES SUPPLEMENTAIRES
-- =====================================================

-- Région Europe
INSERT INTO region (id, nom, code) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Europe', 'EUR'),
    ('a1000000-0000-0000-0000-000000000002', 'Amérique', 'AME');

-- Zones supplémentaires
INSERT INTO zone (id, nom, region_id) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'France', 'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000002', 'Belgique', 'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000003', 'USA', 'a1000000-0000-0000-0000-000000000002'),
    ('b1000000-0000-0000-0000-000000000004', 'Cameroun', (SELECT id FROM region WHERE code = 'AFR'));

-- =====================================================
-- EGLISES LOCALES
-- =====================================================

-- Récupérer l'ID de la zone RDC
INSERT INTO eglise_locale (id, nom, zone_id, adresse) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'Église CMCI Paris Centre', 'b1000000-0000-0000-0000-000000000001', '15 Rue de la Paix, 75002 Paris'),
    ('c1000000-0000-0000-0000-000000000002', 'Église CMCI Lyon', 'b1000000-0000-0000-0000-000000000001', '25 Avenue Jean Jaurès, 69007 Lyon'),
    ('c1000000-0000-0000-0000-000000000003', 'Église CMCI Bruxelles', 'b1000000-0000-0000-0000-000000000002', '10 Rue Royale, 1000 Bruxelles'),
    ('c1000000-0000-0000-0000-000000000004', 'Église CMCI Douala', 'b1000000-0000-0000-0000-000000000004', 'Boulevard de la Liberté, Douala');

-- Mettre à jour l'église de Kinshasa avec un ID fixe
UPDATE eglise_locale SET id = 'c1000000-0000-0000-0000-000000000000' WHERE nom = 'Église CMCI Kinshasa';

-- =====================================================
-- UTILISATEURS (Admin, Pasteurs, Leaders, FDs, Fidèles)
-- =====================================================

-- Admin
INSERT INTO utilisateur (id, email, nom, prenom, role, statut, telephone) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'admin@cmci.org', 'CMCI', 'Admin', 'ADMIN', 'ACTIF', '+33600000001');

-- Pasteurs (un par église locale)
INSERT INTO utilisateur (id, email, nom, prenom, role, statut, telephone, date_bapteme) VALUES
    ('d1000000-0000-0000-0000-000000000002', 'pasteur@cmci.org', 'PASTEUR', 'Jean', 'PASTEUR', 'ACTIF', '+33600000002', '2000-05-15'),
    ('d1000000-0000-0000-0000-000000000003', 'pasteur.lyon@cmci.org', 'DUPONT', 'Marc', 'PASTEUR', 'ACTIF', '+33600000003', '1998-03-20'),
    ('d1000000-0000-0000-0000-000000000004', 'pasteur.bruxelles@cmci.org', 'MARTIN', 'Joseph', 'PASTEUR', 'ACTIF', '+32400000001', '1995-07-10'),
    ('d1000000-0000-0000-0000-000000000005', 'pasteur.douala@cmci.org', 'KAMGA', 'Samuel', 'PASTEUR', 'ACTIF', '+237600000001', '1992-12-01');

-- Mettre à jour les églises avec leurs pasteurs
UPDATE eglise_locale SET pasteur_id = 'd1000000-0000-0000-0000-000000000002' WHERE id = 'c1000000-0000-0000-0000-000000000001';
UPDATE eglise_locale SET pasteur_id = 'd1000000-0000-0000-0000-000000000003' WHERE id = 'c1000000-0000-0000-0000-000000000002';
UPDATE eglise_locale SET pasteur_id = 'd1000000-0000-0000-0000-000000000004' WHERE id = 'c1000000-0000-0000-0000-000000000003';
UPDATE eglise_locale SET pasteur_id = 'd1000000-0000-0000-0000-000000000005' WHERE id = 'c1000000-0000-0000-0000-000000000004';

-- =====================================================
-- EGLISES DE MAISON (plusieurs par église locale)
-- =====================================================

INSERT INTO eglise_maison (id, nom, eglise_locale_id, adresse) VALUES
    -- Paris Centre
    ('e1000000-0000-0000-0000-000000000001', 'EM Paris 11ème', 'c1000000-0000-0000-0000-000000000001', '45 Rue Oberkampf, 75011 Paris'),
    ('e1000000-0000-0000-0000-000000000002', 'EM Paris 18ème', 'c1000000-0000-0000-0000-000000000001', '12 Rue Marcadet, 75018 Paris'),
    ('e1000000-0000-0000-0000-000000000003', 'EM Paris 20ème', 'c1000000-0000-0000-0000-000000000001', '8 Rue de Bagnolet, 75020 Paris'),
    -- Lyon
    ('e1000000-0000-0000-0000-000000000004', 'EM Lyon Villeurbanne', 'c1000000-0000-0000-0000-000000000002', '30 Rue de la République, Villeurbanne'),
    ('e1000000-0000-0000-0000-000000000005', 'EM Lyon Vénissieux', 'c1000000-0000-0000-0000-000000000002', '15 Avenue de la Division, Vénissieux'),
    -- Bruxelles
    ('e1000000-0000-0000-0000-000000000006', 'EM Bruxelles Ixelles', 'c1000000-0000-0000-0000-000000000003', '25 Rue de la Victoire, Ixelles'),
    -- Douala
    ('e1000000-0000-0000-0000-000000000007', 'EM Douala Akwa', 'c1000000-0000-0000-0000-000000000004', 'Rue Akwa Nord, Douala'),
    ('e1000000-0000-0000-0000-000000000008', 'EM Douala Bonanjo', 'c1000000-0000-0000-0000-000000000004', 'Boulevard de la République, Bonanjo');

-- =====================================================
-- LEADERS (un par église de maison)
-- =====================================================

INSERT INTO utilisateur (id, email, nom, prenom, role, eglise_maison_id, statut, telephone, date_bapteme) VALUES
    ('d1000000-0000-0000-0000-000000000010', 'leader@cmci.org', 'LEADER', 'Pierre', 'LEADER', 'e1000000-0000-0000-0000-000000000001', 'ACTIF', '+33600000010', '2005-04-12'),
    ('d1000000-0000-0000-0000-000000000011', 'leader2@cmci.org', 'BERNARD', 'Thomas', 'LEADER', 'e1000000-0000-0000-0000-000000000002', 'ACTIF', '+33600000011', '2008-09-25'),
    ('d1000000-0000-0000-0000-000000000012', 'leader3@cmci.org', 'LEROY', 'François', 'LEADER', 'e1000000-0000-0000-0000-000000000003', 'ACTIF', '+33600000012', '2010-01-18'),
    ('d1000000-0000-0000-0000-000000000013', 'leader.lyon@cmci.org', 'ROUX', 'Philippe', 'LEADER', 'e1000000-0000-0000-0000-000000000004', 'ACTIF', '+33600000013', '2003-06-30'),
    ('d1000000-0000-0000-0000-000000000014', 'leader.bruxelles@cmci.org', 'JANSSEN', 'Luc', 'LEADER', 'e1000000-0000-0000-0000-000000000006', 'ACTIF', '+32400000010', '2007-11-15'),
    ('d1000000-0000-0000-0000-000000000015', 'leader.douala@cmci.org', 'NKODO', 'Emmanuel', 'LEADER', 'e1000000-0000-0000-0000-000000000007', 'ACTIF', '+237600000010', '2001-02-28');

-- Mettre à jour les églises de maison avec leurs leaders
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000010' WHERE id = 'e1000000-0000-0000-0000-000000000001';
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000011' WHERE id = 'e1000000-0000-0000-0000-000000000002';
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000012' WHERE id = 'e1000000-0000-0000-0000-000000000003';
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000013' WHERE id = 'e1000000-0000-0000-0000-000000000004';
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000014' WHERE id = 'e1000000-0000-0000-0000-000000000006';
UPDATE eglise_maison SET leader_id = 'd1000000-0000-0000-0000-000000000015' WHERE id = 'e1000000-0000-0000-0000-000000000007';

-- =====================================================
-- FDS (Frères/Soeurs Dévoués)
-- =====================================================

INSERT INTO utilisateur (id, email, nom, prenom, role, eglise_maison_id, statut, telephone, date_bapteme) VALUES
    ('d1000000-0000-0000-0000-000000000020', 'fd@cmci.org', 'FD', 'Marie', 'FD', 'e1000000-0000-0000-0000-000000000001', 'ACTIF', '+33600000020', '2012-03-10'),
    ('d1000000-0000-0000-0000-000000000021', 'fd2@cmci.org', 'MOREAU', 'Sophie', 'FD', 'e1000000-0000-0000-0000-000000000001', 'ACTIF', '+33600000021', '2014-08-22'),
    ('d1000000-0000-0000-0000-000000000022', 'fd3@cmci.org', 'PETIT', 'Claire', 'FD', 'e1000000-0000-0000-0000-000000000002', 'ACTIF', '+33600000022', '2011-05-17'),
    ('d1000000-0000-0000-0000-000000000023', 'fd.lyon@cmci.org', 'GARNIER', 'Isabelle', 'FD', 'e1000000-0000-0000-0000-000000000004', 'ACTIF', '+33600000023', '2009-12-05'),
    ('d1000000-0000-0000-0000-000000000024', 'fd.douala@cmci.org', 'MBARGA', 'Ruth', 'FD', 'e1000000-0000-0000-0000-000000000007', 'ACTIF', '+237600000020', '2006-07-14');

-- =====================================================
-- FIDELES
-- =====================================================

INSERT INTO utilisateur (id, email, nom, prenom, role, eglise_maison_id, fd_id, statut, telephone, date_bapteme, date_naissance) VALUES
    -- Fidèles sous FD Marie (fd@cmci.org)
    ('d1000000-0000-0000-0000-000000000030', 'fidele@cmci.org', 'FIDELE', 'Paul', 'FIDELE', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000020', 'ACTIF', '+33600000030', '2020-06-15', '1990-03-25'),
    ('d1000000-0000-0000-0000-000000000031', 'fidele2@cmci.org', 'LAMBERT', 'Antoine', 'FIDELE', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000020', 'ACTIF', '+33600000031', '2019-04-20', '1988-11-12'),
    ('d1000000-0000-0000-0000-000000000032', 'fidele3@cmci.org', 'DUBOIS', 'Julie', 'FIDELE', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000020', 'ACTIF', '+33600000032', '2021-09-10', '1995-07-08'),

    -- Fidèles sous FD Sophie
    ('d1000000-0000-0000-0000-000000000033', 'fidele4@cmci.org', 'MARTINEZ', 'Lucas', 'FIDELE', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000021', 'ACTIF', '+33600000033', '2022-01-25', '1992-05-30'),
    ('d1000000-0000-0000-0000-000000000034', 'fidele5@cmci.org', 'GARCIA', 'Emma', 'FIDELE', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000021', 'ACTIF', '+33600000034', '2020-11-08', '1997-02-14'),

    -- Fidèles sous FD Claire
    ('d1000000-0000-0000-0000-000000000035', 'fidele6@cmci.org', 'THOMAS', 'Nathan', 'FIDELE', 'e1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000022', 'ACTIF', '+33600000035', '2018-07-12', '1985-09-20'),

    -- Fidèles sous FD Isabelle (Lyon)
    ('d1000000-0000-0000-0000-000000000036', 'fidele.lyon@cmci.org', 'FAURE', 'Camille', 'FIDELE', 'e1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000023', 'ACTIF', '+33600000036', '2019-02-28', '1993-12-03'),

    -- Fidèles sous FD Ruth (Douala)
    ('d1000000-0000-0000-0000-000000000037', 'fidele.douala@cmci.org', 'FOTSO', 'David', 'FIDELE', 'e1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000024', 'ACTIF', '+237600000030', '2017-05-19', '1991-08-07'),
    ('d1000000-0000-0000-0000-000000000038', 'fidele2.douala@cmci.org', 'TAMBA', 'Esther', 'FIDELE', 'e1000000-0000-0000-0000-000000000007', 'd1000000-0000-0000-0000-000000000024', 'ACTIF', '+237600000031', '2021-03-14', '1996-04-22');

-- =====================================================
-- COMPTES RENDUS
-- =====================================================

-- CR pour Paul FIDELE (derniers jours)
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, litterature_pages, litterature_total, litterature_titre, priere_autres, confession, jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000030', CURRENT_DATE - INTERVAL '1 day', '1/1', '00:45:00', 5, 'Matthieu', 15, 50, 'Les Voies de Dieu', 3, true, false, 2, true, 'Belle journée de communion avec le Seigneur. Réveil à 6h30, temps de prière intense.', 'SOUMIS', false),
    ('f1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000030', CURRENT_DATE - INTERVAL '2 days', '1/1', '01:00:00', 4, 'Matthieu', 20, 50, 'Les Voies de Dieu', 5, true, true, 1, true, 'Jour de jeûne. Prière prolongée pour la famille.', 'VALIDE', true),
    ('f1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000030', CURRENT_DATE - INTERVAL '3 days', '1/1', '00:30:00', 3, 'Psaumes', 10, 100, 'La Prière', 2, false, false, 0, false, 'Journée ordinaire mais bénie.', 'SOUMIS', true),
    ('f1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000030', CURRENT_DATE - INTERVAL '4 days', '1/1', '00:40:00', 6, 'Jean', 12, 100, 'La Prière', 4, true, false, 3, true, 'Évangélisation au marché. 3 personnes contactées.', 'VALIDE', true);

-- CR pour Antoine LAMBERT
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, litterature_pages, litterature_total, litterature_titre, priere_autres, confession, jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000010', 'd1000000-0000-0000-0000-000000000031', CURRENT_DATE - INTERVAL '1 day', '1/1', '01:15:00', 8, 'Romains', 25, 200, 'Connaitre Dieu', 6, true, false, 1, true, 'Réveil tôt, temps de qualité avec Dieu. Lecture approfondie de Romains 8.', 'SOUMIS', false),
    ('f1000000-0000-0000-0000-000000000011', 'd1000000-0000-0000-0000-000000000031', CURRENT_DATE - INTERVAL '2 days', '1/1', '00:50:00', 5, 'Romains', 18, 200, 'Connaitre Dieu', 4, true, true, 0, true, 'Jeûne de 24h. Expérience spirituelle profonde.', 'VALIDE', true);

-- CR pour Julie DUBOIS
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, priere_autres, confession, jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000020', 'd1000000-0000-0000-0000-000000000032', CURRENT_DATE - INTERVAL '1 day', '1/1', '00:35:00', 4, 'Proverbes', 2, true, false, 1, true, 'Méditation sur la sagesse. Partage avec une collègue.', 'SOUMIS', false);

-- CR pour David FOTSO (Douala)
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, litterature_pages, litterature_total, litterature_titre, priere_autres, confession, jeune, type_jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000030', 'd1000000-0000-0000-0000-000000000037', CURRENT_DATE - INTERVAL '1 day', '1/1', '01:30:00', 10, 'Actes', 30, 150, 'La Marche Chrétienne', 8, true, true, 'Jeûne complet', 5, true, 'Réveil de prière à 4h30. Évangélisation dans le quartier. 5 personnes touchées par la Parole.', 'SOUMIS', false),
    ('f1000000-0000-0000-0000-000000000031', 'd1000000-0000-0000-0000-000000000037', CURRENT_DATE - INTERVAL '2 days', '1/1', '01:00:00', 7, 'Actes', 20, 150, 'La Marche Chrétienne', 5, true, false, NULL, 3, true, 'Continuation de la lecture des Actes. Prière pour le réveil.', 'VALIDE', true);

-- CR pour Esther TAMBA
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, priere_autres, confession, jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000038', CURRENT_DATE - INTERVAL '1 day', '0/1', '00:55:00', 6, 'Éphésiens', 4, true, false, 2, true, 'Étude sur l armure de Dieu. Témoignage à deux voisines.', 'SOUMIS', false);

-- CR pour Camille FAURE (Lyon)
INSERT INTO compte_rendu (id, utilisateur_id, date, rdqd, priere_seule, lecture_biblique, livre_biblique, litterature_pages, litterature_total, litterature_titre, priere_autres, confession, jeune, evangelisation, offrande, notes, statut, vu_par_fd) VALUES
    ('f1000000-0000-0000-0000-000000000050', 'd1000000-0000-0000-0000-000000000036', CURRENT_DATE - INTERVAL '1 day', '1/1', '00:45:00', 5, '1 Corinthiens', 15, 80, 'La Vie Spirituelle', 3, true, false, 1, true, 'Méditation sur l amour (1 Cor 13). Application pratique dans la journée.', 'SOUMIS', false);

-- =====================================================
-- COMMENTAIRES SUR LES COMPTES RENDUS
-- =====================================================

-- Commentaires de FD Marie sur les CR de Paul
INSERT INTO commentaire_cr (id, compte_rendu_id, auteur_id, contenu) VALUES
    ('c0100000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000020', 'Excellent témoignage de consécration ! Le jeûne et la prière prolongée montrent une vraie soif de Dieu. Continue ainsi frère Paul !'),
    ('c0100000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000003', 'd1000000-0000-0000-0000-000000000020', 'Merci pour ta fidélité. Même les journées "ordinaires" comptent dans notre marche avec le Seigneur.'),
    ('c0100000-0000-0000-0000-000000000003', 'f1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000020', 'Bravo pour l évangélisation ! 3 personnes contactées, c est un beau fruit. Que le Seigneur continue à t utiliser.');

-- Commentaires de FD Sophie sur les CR d'Antoine
INSERT INTO commentaire_cr (id, compte_rendu_id, auteur_id, contenu) VALUES
    ('c0100000-0000-0000-0000-000000000010', 'f1000000-0000-0000-0000-000000000011', 'd1000000-0000-0000-0000-000000000021', 'Le jeûne de 24h est un sacrifice agréable au Seigneur. Ton expérience spirituelle profonde est un encouragement pour nous tous.');

-- Commentaires du Leader Pierre
INSERT INTO commentaire_cr (id, compte_rendu_id, auteur_id, contenu) VALUES
    ('c0100000-0000-0000-0000-000000000020', 'f1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000010', 'Excellent frère ! Tu es un exemple pour l église de maison. Le Seigneur bénit ta consécration.'),
    ('c0100000-0000-0000-0000-000000000021', 'f1000000-0000-0000-0000-000000000004', 'd1000000-0000-0000-0000-000000000010', 'L évangélisation est le coeur de notre mission. Que Dieu multiplie les occasions !');

-- Commentaires de FD Ruth sur les CR de David et Esther (Douala)
INSERT INTO commentaire_cr (id, compte_rendu_id, auteur_id, contenu) VALUES
    ('c0100000-0000-0000-0000-000000000030', 'f1000000-0000-0000-0000-000000000031', 'd1000000-0000-0000-0000-000000000024', 'Ton zèle pour la Parole et l évangélisation est remarquable David. Le réveil à 4h30 montre ta détermination. Gloire à Dieu !'),
    ('c0100000-0000-0000-0000-000000000031', 'f1000000-0000-0000-0000-000000000040', 'd1000000-0000-0000-0000-000000000024', 'Très bien Esther ! L étude sur l armure de Dieu est fondamentale pour notre combat spirituel.');

-- Commentaire du Pasteur sur un CR validé
INSERT INTO commentaire_cr (id, compte_rendu_id, auteur_id, contenu) VALUES
    ('c0100000-0000-0000-0000-000000000040', 'f1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000002', 'Paul, ton engagement dans le jeûne et la prière est un témoignage puissant. Je t encourage à continuer et à partager ton expérience avec les autres frères.');

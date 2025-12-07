-- 1. Assurer que les TYPES existent (ON CONFLICT évite l'erreur si déjà présents)
INSERT INTO event_type (event_type_id, label) VALUES 
('11111111-1111-1111-1111-111111111111', 'Concert'),
('22222222-2222-2222-2222-222222222222', 'Festival'),
('33333333-3333-3333-3333-333333333333', 'Sport'),
('44444444-4444-4444-4444-444444444444', 'Théâtre')
ON CONFLICT (event_type_id) DO NOTHING;

-- 2. Assurer que les CATÉGORIES existent
INSERT INTO event_category (category_id, label) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Musique'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Football'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Arts'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Technologie')
ON CONFLICT (category_id) DO NOTHING;

-- 3. Insérer les Événements (Maintenant que les clés étrangères existent)
INSERT INTO event (event_id, name, description, start_date, end_date, location, full_address, image_url, status, creator_id, creation_date, event_type_id, category_id) VALUES 
(
    'e4e4e4e4-e4e4-e4e4-e4e4-e4e4e4e4e4e4', 
    'Grand Concert de Noël', 
    'Une soirée magique avec orchestre symphonique pour célébrer les fêtes.', 
    '2025-12-20 20:00:00', 
    '2025-12-20 23:00:00', 
    'Paris', 
    'Philharmonie de Paris, 221 Avenue Jean Jaurès, 75019 Paris', 
    'https://images.unsplash.com/photo-1514525253440-b393452e3383?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072', -- ID Super Admin
    NOW(),
    '11111111-1111-1111-1111-111111111111', -- Concert (Ça marchera maintenant !)
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 
    'Nouvel An sur les Champs', 
    'Célébration du passage à la nouvelle année avec DJ set et feux d''artifice.', 
    '2025-12-31 22:00:00', 
    '2026-01-01 02:00:00', 
    'Paris', 
    'Avenue des Champs-Élysées, 75008 Paris', 
    'https://images.unsplash.com/photo-1467810563316-b5476525c0f9?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '22222222-2222-2222-2222-222222222222', 
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    'e6e6e6e6-e6e6-e6e6-e6e6-e6e6e6e6e6e6', 
    'OL vs ASSE - Le Derby', 
    'Le match le plus attendu de la saison au Groupama Stadium.', 
    '2026-01-10 21:00:00', 
    '2026-01-10 23:00:00', 
    'Lyon', 
    '10 Avenue Simone Veil, 69150 Décines-Charpieu', 
    'https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '33333333-3333-3333-3333-333333333333', 
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    'e7e7e7e7-e7e7-e7e7-e7e7-e7e7e7e7e7e7', 
    'Le Malade Imaginaire', 
    'Représentation exceptionnelle de la pièce de Molière par la Comédie Française.', 
    '2026-01-15 19:30:00', 
    '2026-01-15 21:30:00', 
    'Bordeaux', 
    'Grand Théâtre, Place de la Comédie, 33000 Bordeaux', 
    'https://images.unsplash.com/photo-1503095392269-41f86433c393?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '44444444-4444-4444-4444-444444444444', 
    'cccccccc-cccc-cccc-cccc-cccccccccccc'
),
(
    'e8e8e8e8-e8e8-e8e8-e8e8-e8e8e8e8e8e8', 
    'AI Summit 2026', 
    'Conférence mondiale sur l''intelligence artificielle et la robotique.', 
    '2026-01-25 09:00:00', 
    '2026-01-27 18:00:00', 
    'Toulouse', 
    'MEETT, Concorde Avenue, 31840 Aussonne', 
    'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '22222222-2222-2222-2222-222222222222', 
    'dddddddd-dddd-dddd-dddd-dddddddddddd'
),
(
    'e9e9e9e9-e9e9-e9e9-e9e9-e9e9e9e9e9e9', 
    'Exposition : Lumières du Sud', 
    'Vernissage de la nouvelle collection d''art contemporain.', 
    '2026-02-05 18:00:00', 
    '2026-02-05 22:00:00', 
    'Marseille', 
    'MuCEM, 7 Prom. Robert Laffont, 13002 Marseille', 
    'https://images.unsplash.com/photo-1536924940846-227afb31e2a5?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '22222222-2222-2222-2222-222222222222', 
    'cccccccc-cccc-cccc-cccc-cccccccccccc'
),
(
    '0a0a0a0a-0a0a-0a0a-0a0a-0a0a0a0a0a0a', 
    'Concert Rock Legends', 
    'Hommage aux plus grands groupes de rock des années 80.', 
    '2026-02-14 20:30:00', 
    '2026-02-14 23:30:00', 
    'Lille', 
    'Zénith de Lille, 1 Boulevard des Cités Unies, 59777 Lille', 
    'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '11111111-1111-1111-1111-111111111111', 
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    '0b0b0b0b-0b0b-0b0b-0b0b-0b0b0b0b0b0b', 
    'Hackathon GreenCode', 
    '48h pour développer des solutions écologiques.', 
    '2026-02-20 18:00:00', 
    '2026-02-22 18:00:00', 
    'Nantes', 
    'La Cité des Congrès, 5 Rue de Valmy, 44000 Nantes', 
    'https://images.unsplash.com/photo-1504384308090-c54be3855833?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '22222222-2222-2222-2222-222222222222', 
    'dddddddd-dddd-dddd-dddd-dddddddddddd'
),
(
    '0c0c0c0c-0c0c-0c0c-0c0c-0c0c0c0c0c0c', 
    'Finale Coupe Régionale', 
    'Venez supporter votre équipe locale pour la grande finale.', 
    '2026-03-15 15:00:00', 
    '2026-03-15 17:00:00', 
    'Strasbourg', 
    'Stade de la Meinau, 12 Rue de l''Extenwoerth, 67100 Strasbourg', 
    'https://images.unsplash.com/photo-1522778119026-d647f0565c71?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '33333333-3333-3333-3333-333333333333', 
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    '0d0d0d0d-0d0d-0d0d-0d0d-0d0d0d0d0d0d', 
    'Stand-up Comedy Night', 
    'Les meilleurs humoristes de la scène française réunis pour un soir.', 
    '2026-03-20 21:00:00', 
    '2026-03-20 23:00:00', 
    'Montpellier', 
    'Le Corum, Place Charles de Gaulle, 34000 Montpellier', 
    'https://images.unsplash.com/photo-1585699324551-f6c309eedca2?q=80&w=1000&auto=format&fit=crop', 
    'active', 
    '6153bdef-8cb5-4e7c-a286-c1d382fb6072',
    NOW(),
    '44444444-4444-4444-4444-444444444444', 
    'cccccccc-cccc-cccc-cccc-cccccccccccc'
);
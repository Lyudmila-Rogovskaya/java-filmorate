INSERT INTO mpa_ratings (mpa_id, code, description) VALUES
(1, 'G', 'General Audiences'),
(2, 'PG', 'Parental Guidance Suggested'),
(3, 'PG-13', 'Parents Strongly Cautioned'),
(4, 'R', 'Restricted'),
(5, 'NC-17', 'Adults Only');

INSERT INTO genres (genre_id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

MERGE INTO mpa_ratings (mpa_id, code, description)
VALUES (1, 'G', 'General Audiences'), ...;

MERGE INTO genres (genre_id, name)
VALUES (1, 'Комедия'), ...;

MERGE INTO mpa_ratings (mpa_id, name, description)
VALUES (1, 'G', 'General Audiences'),
       (2, 'PG', 'Parental Guidance Suggested'),
       (3, 'PG-13', 'Parents Strongly Cautioned'),
       (4, 'R', 'Restricted'),
       (5, 'NC-17', 'Adults Only');

MERGE INTO genres (genre_id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');


DELETE FROM users;
INSERT INTO users (id, username, password, full_name, creation_date, updated_at, version) VALUES
 (1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRZmUXVXY78HP8pcLVsBDWamQUu', 'Alex Morgan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
 (2, 'programmer1', '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRZmUXVXY78HP8pcLVsBDWamQUu', 'Bianca Reed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
 (3, 'staff1', '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRZmUXVXY78HP8pcLVsBDWamQUu', 'Chris Taylor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
 (4, 'submitter1', '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRZmUXVXY78HP8pcLVsBDWamQUu', 'Dana Wilson', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


DELETE FROM programs;
INSERT INTO programs (id, name, description, start_date, end_date, state, creator_id, creation_date, updated_at, version) VALUES
(1, 'Horror March', 'A month dedicated to the best horror films of the decade.', '2026-03-01', '2026-03-31', 'ANNOUNCED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'Crime Classics', 'Exploring the gritty underworld of cinema through legendary crime dramas.', '2026-10-01', '2026-10-31', 'ANNOUNCED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'Gotham Nights', 'A celebration of the Dark Knight and the stories of Gotham City.', '2026-11-01', '2026-11-30', 'ANNOUNCED', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 'Family Sagas', 'Deep dives into the complex relationships and legacies of cinematic families.', '2026-12-01', '2026-12-31', 'ANNOUNCED', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5, 'Legal Dramas', 'Tense moments and moral dilemmas from the courtroom.', '2027-01-01', '2027-01-31', 'DECISION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(6, 'Historical Perspectives', 'Films that reflect on our past and the human spirit during conflict.', '2027-02-01', '2027-02-28', 'DECISION', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(7, 'Epic Journeys', 'Grand adventures through fantastical worlds and unknown territories.', '2027-03-01', '2027-03-31', 'DECISION', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(8, 'Interconnected Lives', 'Stories where paths cross in unexpected and impactful ways.', '2027-04-01', '2027-04-30', 'FINAL_PUBLICATION', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(9, 'Fantasy Realms', 'Escaping reality through high fantasy and magical lore.', '2027-05-01', '2027-05-31', 'FINAL_PUBLICATION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(10, 'Western Frontier', 'Classic and modern takes on the myths and realities of the Old West.', '2027-06-01', '2027-06-30', 'FINAL_PUBLICATION', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(11, 'Americana', 'Portraits of American life, history, and the pursuit of dreams.', '2027-07-01', '2027-07-31', 'FINAL_PUBLICATION', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(12, 'Identity Crisis', 'Exploring the psyche and the search for self in a modern world.', '2027-08-01', '2027-08-31', 'SCHEDULING', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(13, 'Mind Benders', 'Cerebral thrillers that challenge our perception of reality.', '2027-09-01', '2027-09-30', 'SCHEDULING', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(14, 'Divided Loyalties', 'Struggles for power and the bonds that hold us together or tear us apart.', '2027-10-01', '2027-10-31', 'SCHEDULING', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(15, 'Cyberpunk Futures', 'Humanity vs. Technology in dystopian realities.', '2027-11-01', '2027-11-30', 'SCHEDULING', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(16, 'Rise and Fall', 'The seductive allure and inevitable collapse of criminal power.', '2028-01-01', '2028-01-31', 'REVIEW', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(17, 'Institution and Rebellion', 'Challenging authority and the struggle for personal freedom.', '2028-02-01', '2028-02-29', 'REVIEW', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(18, 'Cosmic Horizons', 'Exploring the vastness of space and the future of humanity.', '2028-03-01', '2028-03-31', 'REVIEW', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(19, 'Honor and Duty', 'Timeless stories of sacrifice, protection, and the warrior code.', '2028-04-01', '2028-04-30', 'REVIEW', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(20, 'Second Chances', 'Life-affirming stories of redemption and what truly matters.', '2028-05-01', '2028-05-31', 'ASSIGNMENT', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(21, 'Psychological Thrillers', 'Peering into the dark corners of the human mind.', '2028-06-01', '2028-06-30', 'ASSIGNMENT', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(22, 'War and Remembrance', 'Honoring the sacrifices and stories from the front lines.', '2028-07-01', '2028-07-31', 'ASSIGNMENT', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(23, 'Urban Realities', 'The challenges and triumphs of life in the modern city.', '2028-08-01', '2028-08-31', 'ASSIGNMENT', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(24, 'Fable and Fact', 'Using imagination to navigate the harsh realities of life.', '2028-09-01', '2028-09-30', 'SUBMISSION', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(25, 'Supernatural Suspense', 'Stories that touch on the inexplicable and the afterlife.', '2028-10-01', '2028-10-31', 'SUBMISSION', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(26, 'Sci-Fi Action', 'High-stakes battles and advanced technology from the future.', '2028-11-01', '2028-11-30', 'SUBMISSION', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(27, 'Time Travel Adventures', 'Zipping through history and dealing with the consequences.', '2029-01-01', '2029-01-31', 'CREATED', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(28, 'Animated Worlds', 'Stories brought to life through extraordinary animation.', '2029-02-01', '2029-02-28', 'CREATED', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(29, 'Biographical Portraits', 'The true stories of individuals who shaped our world.', '2029-03-01', '2029-03-31', 'CREATED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(30, 'Roman History', 'Epics from the height of the Roman Empire.', '2029-04-01', '2029-04-30', 'CREATED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


DELETE FROM program_programmers;
INSERT INTO program_programmers (program_id, user_id) VALUES
 (1,1),(2,2),(3,3),(4,4),(5,1),(6,2),(7,3),(8,4),(9,1),(10,2),
 (11,3),(12,4),(13,1),(14,2),(15,3),(16,4),(17,1),(18,2),(19,3),(20,4),
 (21,1),(22,2),(23,3),(24,4),(25,1),(26,2),(27,3),(28,4),(29,1),(30,2);

DELETE FROM program_staff;
INSERT INTO program_staff (program_id, user_id) VALUES
 (1,2),(2,3),(3,4),(4,1),(5,2),(6,3),(7,4),(8,1),(9,2),(10,3),
 (11,4),(12,1),(13,2),(14,3),(15,4),(16,1),(17,2),(18,3),(19,4),(20,1),
 (21,2),(22,3),(23,4),(24,1),(25,2),(26,3),(27,4),(28,1),(29,2),(30,3);


CREATE TABLE IF NOT EXISTS films (
 id BIGINT PRIMARY KEY, title VARCHAR(255) NOT NULL UNIQUE, film_cast VARCHAR(255), genres VARCHAR(255),
 duration INTEGER, creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, version BIGINT DEFAULT 0
);
DELETE FROM films;

INSERT INTO films (id, title, film_cast, genres, duration, creation_date) VALUES
(1, 'The Shawshank Redemption', 'Tim Robbins, Morgan Freeman', 'Drama', 142, CURRENT_TIMESTAMP),
(2, 'The Godfather', 'Marlon Brando, Al Pacino', 'Crime, Drama', 175, CURRENT_TIMESTAMP),
(3, 'The Dark Knight', 'Christian Bale, Heath Ledger', 'Action, Crime', 152, CURRENT_TIMESTAMP),
(4, 'The Godfather Part II', 'Al Pacino, Robert De Niro', 'Crime, Drama', 202, CURRENT_TIMESTAMP),
(5, '12 Angry Men', 'Henry Fonda, Lee J. Cobb', 'Drama', 96, CURRENT_TIMESTAMP),
(6, 'Schindler''s List', 'Liam Neeson, Ben Kingsley', 'Drama, History', 195, CURRENT_TIMESTAMP),
(7, 'The Lord of the Rings: The Return of the King', 'Elijah Wood, Viggo Mortensen', 'Adventure, Fantasy', 252, CURRENT_TIMESTAMP),
(8, 'Pulp Fiction', 'John Travolta, Samuel L. Jackson', 'Crime, Drama', 154, CURRENT_TIMESTAMP),
(9, 'The Lord of the Rings: The Fellowship of the Ring', 'Elijah Wood, Ian McKellen', 'Adventure, Fantasy', 178, CURRENT_TIMESTAMP),
(10, 'The Good, the Bad and the Ugly', 'Clint Eastwood, Eli Wallach', 'Western', 178, CURRENT_TIMESTAMP),
(11, 'Forrest Gump', 'Tom Hanks, Robin Wright', 'Drama, Romance', 142, CURRENT_TIMESTAMP),
(12, 'Fight Club', 'Brad Pitt, Edward Norton', 'Drama', 139, CURRENT_TIMESTAMP),
(13, 'Inception', 'Leonardo DiCaprio, Joseph Gordon-Levitt', 'Action, Sci-Fi', 148, CURRENT_TIMESTAMP),
(14, 'The Lord of the Rings: The Two Towers', 'Elijah Wood, Ian McKellen', 'Adventure, Fantasy', 179, CURRENT_TIMESTAMP),
(15, 'The Matrix', 'Keanu Reeves, Laurence Fishburne', 'Action, Sci-Fi', 136, CURRENT_TIMESTAMP),
(16, 'Goodfellas', 'Robert De Niro, Ray Liotta', 'Crime, Drama', 146, CURRENT_TIMESTAMP),
(17, 'One Flew Over the Cuckoo''s Nest', 'Jack Nicholson, Louise Fletcher', 'Drama', 133, CURRENT_TIMESTAMP),
(18, 'Interstellar', 'Matthew McConaughey, Anne Hathaway', 'Adventure, Sci-Fi', 169, CURRENT_TIMESTAMP),
(19, 'Seven Samurai', 'Toshirô Mifune, Takashi Shimura', 'Action, Drama', 207, CURRENT_TIMESTAMP),
(20, 'It''s a Wonderful Life', 'James Stewart, Donna Reed', 'Drama, Family', 130, CURRENT_TIMESTAMP),
(21, 'The Silence of the Lambs', 'Jodie Foster, Anthony Hopkins', 'Crime, Thriller', 118, CURRENT_TIMESTAMP),
(22, 'Saving Private Ryan', 'Tom Hanks, Matt Damon', 'Drama, War', 169, CURRENT_TIMESTAMP),
(23, 'City of God', 'Alexandre Rodrigues, Leandro Firmino', 'Crime, Drama', 130, CURRENT_TIMESTAMP),
(24, 'Life Is Beautiful', 'Roberto Benigni, Nicoletta Braschi', 'Comedy, Drama', 116, CURRENT_TIMESTAMP),
(25, 'The Green Mile', 'Tom Hanks, Michael Clarke Duncan', 'Crime, Drama', 189, CURRENT_TIMESTAMP),
(26, 'Terminator 2: Judgment Day', 'Arnold Schwarzenegger, Linda Hamilton', 'Action, Sci-Fi', 137, CURRENT_TIMESTAMP),
(27, 'Back to the Future', 'Michael J. Fox, Christopher Lloyd', 'Adventure, Comedy', 116, CURRENT_TIMESTAMP),
(28, 'Spirited Away', 'Rumi Hiiragi, Miyu Irino', 'Animation, Fantasy', 125, CURRENT_TIMESTAMP),
(29, 'The Pianist', 'Adrien Brody, Thomas Kretschmann', 'Biography, Drama', 150, CURRENT_TIMESTAMP),
(30, 'Gladiator', 'Russell Crowe, Joaquin Phoenix', 'Action, Drama', 155, CURRENT_TIMESTAMP);


DELETE FROM screenings;
INSERT INTO screenings (id, name, program_id, state, start_time, end_time, film_id, auditorium_name, submitter_id, creation_date, finally_submitted, handler_id) VALUES
 (1,'Shawshank Opening',1,'SCHEDULED','2026-09-05 18:00:00','2026-09-05 20:22:00',1,'Hall A',3,CURRENT_TIMESTAMP,TRUE,2),
 (2,'Shawshank Encore',1,'SCHEDULED','2026-09-12 18:00:00','2026-09-12 20:22:00',1,'Hall A',4,CURRENT_TIMESTAMP,TRUE,2),
 (3,'Hope and Redemption',1,'SCHEDULED','2026-09-20 15:00:00','2026-09-20 17:22:00',1,'Hall B',3,CURRENT_TIMESTAMP,TRUE,2),
 (4,'Godfather Opening',2,'SCHEDULED','2026-10-03 18:00:00','2026-10-03 20:55:00',2,'Hall A',1,CURRENT_TIMESTAMP,TRUE,3),
 (5,'Godfather Encore',2,'SCHEDULED','2026-10-15 18:00:00','2026-10-15 20:55:00',2,'Hall A',4,CURRENT_TIMESTAMP,TRUE,3),
 (6,'Family and Power',2,'SCHEDULED','2026-10-24 15:00:00','2026-10-24 17:55:00',2,'Hall C',1,CURRENT_TIMESTAMP,TRUE,3),
 (7,'Dark Knight Opening',3,'SCHEDULED','2026-11-06 18:00:00','2026-11-06 20:32:00',3,'Hall A',1,CURRENT_TIMESTAMP,TRUE,4),
 (8,'Gotham Encore',3,'SCHEDULED','2026-11-14 18:00:00','2026-11-14 20:32:00',3,'Hall A',2,CURRENT_TIMESTAMP,TRUE,4),
 (9,'The Final Hour',3,'SCHEDULED','2026-11-22 15:00:00','2026-11-22 17:32:00',3,'Hall D',1,CURRENT_TIMESTAMP,TRUE,4),
 (10,'Godfather Part II Opening',4,'SCHEDULED','2026-12-04 18:00:00','2026-12-04 21:22:00',4,'Hall A',2,CURRENT_TIMESTAMP,TRUE,1),
 (11,'A Family Legacy',4,'SCHEDULED','2026-12-13 18:00:00','2026-12-13 21:22:00',4,'Hall A',3,CURRENT_TIMESTAMP,TRUE,1),
 (12,'Part II Encore',4,'SCHEDULED','2026-12-21 14:00:00','2026-12-21 17:22:00',4,'Hall B',2,CURRENT_TIMESTAMP,TRUE,1),
 (13,'Jury Deliberation',5,'APPROVED','2027-01-08 18:00:00','2027-01-08 19:36:00',5,'Hall A',3,CURRENT_TIMESTAMP,TRUE,2),
 (14,'A Reasonable Doubt',5,'REJECTED',NULL,NULL,5,'Hall B',4,CURRENT_TIMESTAMP,FALSE,2),
 (15,'Schindler Screening',6,'APPROVED','2027-02-12 18:00:00','2027-02-12 21:15:00',6,'Hall C',4,CURRENT_TIMESTAMP,TRUE,3),
 (16,'Schindler Alternate',6,'REJECTED',NULL,NULL,6,'Hall D',1,CURRENT_TIMESTAMP,FALSE,3),
 (17,'Return of the King',7,'SCHEDULED','2027-03-07 17:00:00','2027-03-07 21:12:00',7,'Hall A',1,CURRENT_TIMESTAMP,TRUE,4),
 (18,'Middle-earth Debate',7,'REJECTED',NULL,NULL,7,'Hall B',2,CURRENT_TIMESTAMP,FALSE,4),
 (19,'Pulp Fiction Cut',8,'APPROVED',NULL,NULL,8,'Hall A',1,CURRENT_TIMESTAMP,FALSE,1),
 (20,'Pulp Fiction Reserve',8,'APPROVED',NULL,NULL,8,'Hall B',2,CURRENT_TIMESTAMP,FALSE,1),
 (21,'Fellowship Cut',9,'APPROVED',NULL,NULL,9,'Hall A',3,CURRENT_TIMESTAMP,FALSE,2),
 (22,'Fellowship Reserve',9,'APPROVED',NULL,NULL,9,'Hall B',4,CURRENT_TIMESTAMP,FALSE,2),
 (23,'Western Showcase',10,'APPROVED',NULL,NULL,10,'Hall C',4,CURRENT_TIMESTAMP,FALSE,3),
 (24,'Forrest Showcase',11,'APPROVED',NULL,NULL,11,'Hall D',1,CURRENT_TIMESTAMP,FALSE,4),
 (25,'Fight Club Review',12,'APPROVED','2027-08-08 18:00:00','2027-08-08 20:19:00',12,'Hall A',3,CURRENT_TIMESTAMP,FALSE,1),
 (26,'Fight Club Reject',12,'REJECTED',NULL,NULL,12,'Hall B',4,CURRENT_TIMESTAMP,FALSE,1),
 (27,'Inception A',13,'APPROVED','2027-09-10 18:00:00','2027-09-10 20:28:00',13,'Hall A',4,CURRENT_TIMESTAMP,FALSE,2),
 (28,'Inception B',13,'REVIEWED','2027-09-18 15:00:00','2027-09-18 17:28:00',13,'Hall B',3,CURRENT_TIMESTAMP,FALSE,2),
 (29,'Two Towers A',14,'APPROVED','2027-10-09 18:00:00','2027-10-09 20:59:00',14,'Hall C',1,CURRENT_TIMESTAMP,FALSE,3),
 (30,'Two Towers B',14,'REJECTED',NULL,NULL,14,'Hall D',4,CURRENT_TIMESTAMP,FALSE,3),
 (31,'Matrix A',15,'REVIEWED','2027-11-12 18:00:00','2027-11-12 20:16:00',15,'Hall A',2,CURRENT_TIMESTAMP,FALSE,4),
 (32,'Matrix B',15,'APPROVED','2027-11-20 15:00:00','2027-11-20 17:16:00',15,'Hall B',1,CURRENT_TIMESTAMP,FALSE,4),
 (33,'Goodfellas Review',16,'REVIEWED',NULL,NULL,16,'Hall A',1,CURRENT_TIMESTAMP,FALSE,1),
 (34,'Goodfellas Second Look',16,'SUBMITTED',NULL,NULL,16,'Hall B',2,CURRENT_TIMESTAMP,FALSE,1),
 (35,'Cuckoo Review',17,'REVIEWED',NULL,NULL,17,'Hall C',3,CURRENT_TIMESTAMP,FALSE,2),
 (36,'Cuckoo Submission',17,'SUBMITTED',NULL,NULL,17,'Hall D',4,CURRENT_TIMESTAMP,FALSE,2),
 (37,'Interstellar Review',18,'REVIEWED',NULL,NULL,18,'Hall A',4,CURRENT_TIMESTAMP,FALSE,3),
 (38,'Interstellar Submission',18,'SUBMITTED',NULL,NULL,18,'Hall B',1,CURRENT_TIMESTAMP,FALSE,3),
 (39,'Seven Samurai Review',19,'REVIEWED',NULL,NULL,19,'Hall C',1,CURRENT_TIMESTAMP,FALSE,4),
 (40,'Seven Samurai Submission',19,'SUBMITTED',NULL,NULL,19,'Hall D',2,CURRENT_TIMESTAMP,FALSE,4),
 (41,'Wonderful Life A',20,'SUBMITTED',NULL,NULL,20,'Hall A',3,CURRENT_TIMESTAMP,FALSE,1),
 (42,'Wonderful Life B',20,'SUBMITTED',NULL,NULL,20,'Hall B',4,CURRENT_TIMESTAMP,FALSE,1),
 (43,'Silence A',21,'SUBMITTED',NULL,NULL,21,'Hall C',4,CURRENT_TIMESTAMP,FALSE,2),
 (44,'Silence B',21,'SUBMITTED',NULL,NULL,21,'Hall D',2,CURRENT_TIMESTAMP,FALSE,2),
 (45,'Ryan A',22,'SUBMITTED',NULL,NULL,22,'Hall A',1,CURRENT_TIMESTAMP,FALSE,3),
 (46,'Ryan B',22,'SUBMITTED',NULL,NULL,22,'Hall B',4,CURRENT_TIMESTAMP,FALSE,3),
 (47,'City of God A',23,'SUBMITTED',NULL,NULL,23,'Hall C',1,CURRENT_TIMESTAMP,FALSE,4),
 (48,'City of God B',23,'SUBMITTED',NULL,NULL,23,'Hall D',2,CURRENT_TIMESTAMP,FALSE,4),
 (49,'Life Is Beautiful A',24,'SUBMITTED',NULL,NULL,24,'Hall A',1,CURRENT_TIMESTAMP,FALSE,1),
 (50,'Life Is Beautiful B',24,'CREATED',NULL,NULL,24,'Hall B',2,CURRENT_TIMESTAMP,FALSE,1),
 (51,'Green Mile A',25,'SUBMITTED',NULL,NULL,25,'Hall C',3,CURRENT_TIMESTAMP,FALSE,2),
 (52,'Green Mile B',25,'CREATED',NULL,NULL,25,'Hall D',4,CURRENT_TIMESTAMP,FALSE,2),
 (53,'T2 A',26,'SUBMITTED',NULL,NULL,26,'Hall A',3,CURRENT_TIMESTAMP,FALSE,3),
 (54,'T2 B',26,'CREATED',NULL,NULL,26,'Hall B',4,CURRENT_TIMESTAMP,FALSE,3),
 (55,'Future A',27,'CREATED',NULL,NULL,27,'Hall A',1,CURRENT_TIMESTAMP,FALSE,4),
 (56,'Future B',27,'CREATED',NULL,NULL,27,'Hall B',2,CURRENT_TIMESTAMP,FALSE,4),
 (57,'Spirited A',28,'CREATED',NULL,NULL,28,'Hall C',2,CURRENT_TIMESTAMP,FALSE,1),
 (58,'Spirited B',28,'CREATED',NULL,NULL,28,'Hall D',3,CURRENT_TIMESTAMP,FALSE,1),
 (59,'Pianist A',29,'CREATED',NULL,NULL,29,'Hall A',2,CURRENT_TIMESTAMP,FALSE,2),
 (60,'Pianist B',29,'CREATED',NULL,NULL,29,'Hall B',3,CURRENT_TIMESTAMP,FALSE,2),
 (61,'Gladiator A',30,'CREATED',NULL,NULL,30,'Hall C',3,CURRENT_TIMESTAMP,FALSE,3),
 (62,'Gladiator B',30,'CREATED',NULL,NULL,30,'Hall D',4,CURRENT_TIMESTAMP,FALSE,3),
 (63,'Gladiator Matinee',30,'CREATED',NULL,NULL,30,'Hall A',4,CURRENT_TIMESTAMP,FALSE,3),
 (64,'Pulp Fiction Late Show',8,'APPROVED',NULL,NULL,8,'Hall C',2,CURRENT_TIMESTAMP,FALSE,1),
 (65,'Return of the King Matinee',7,'REJECTED',NULL,NULL,7,'Hall C',4,CURRENT_TIMESTAMP,FALSE,4),
 (66,'Inception Late Show',13,'REVIEWED','2027-09-25 18:00:00','2027-09-25 20:28:00',13,'Hall C',4,CURRENT_TIMESTAMP,FALSE,2),
 (67,'Matrix Late Show',15,'APPROVED','2027-11-27 18:00:00','2027-11-27 20:16:00',15,'Hall C',1,CURRENT_TIMESTAMP,FALSE,4),
 (68,'Ryan Special',22,'SUBMITTED',NULL,NULL,22,'Hall A',1,CURRENT_TIMESTAMP,FALSE,3),
 (69,'Green Mile Special',25,'CREATED',NULL,NULL,25,'Hall A',4,CURRENT_TIMESTAMP,FALSE,2),
 (70,'Shawshank Community Show',1,'SCHEDULED','2026-09-27 12:00:00','2026-09-27 14:22:00',1,'Hall C',3,CURRENT_TIMESTAMP,TRUE,2);


DELETE FROM reviews;
INSERT INTO reviews (screening_id, reviewer_id, score, comments, creation_date) VALUES
 (28,2,9,'Excellent construction and ambitious visual design.',CURRENT_TIMESTAMP),
 (31,4,8,'Strong concept with clear audience appeal.',CURRENT_TIMESTAMP),
 (33,1,9,'Outstanding curation fit and technical quality.',CURRENT_TIMESTAMP),
 (35,2,9,'Powerful performances and enduring relevance.',CURRENT_TIMESTAMP),
 (37,3,10,'Visually exceptional and suitable for the large auditorium.',CURRENT_TIMESTAMP),
 (39,4,9,'Landmark action direction with strong educational value.',CURRENT_TIMESTAMP),
 (66,2,8,'A compelling second screening option.',CURRENT_TIMESTAMP);
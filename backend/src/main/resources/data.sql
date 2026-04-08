DELETE FROM staff_details;
DELETE FROM users;
DELETE FROM system_settings;
DELETE FROM books;

INSERT INTO books (title, author, isbn, category, release_year, status, description)
VALUES
('Wiedźmin: Ostatnie życzenie', 'Andrzej Sapkowski', '9788375900989', 'Fantasy', 1993, 'AVAILABLE',
 'Zbiór opowiadań o wiedźminie Geralcie z Rivii.'),

('1984', 'George Orwell', '9788373926516', 'Dystopia', 1949, 'AVAILABLE',
 'Wizja totalitarnej przyszłości pod okiem Wielkiego Brata.'),

('Zbrodnia i kara', 'Fiodor Dostojewski', '9788377794357', 'Klasyka', 1866, 'RENTED',
 'Psychologiczne studium morderstwa i sumienia.'),

('Marsjanin', 'Andy Weir', '9788328701045', 'Sci-Fi', 2011, 'AVAILABLE',
 'Historia astronauty, który musi przetrwać samotnie na Marsie.'),

('Steve Jobs', 'Walter Isaacson', '9788308048122', 'Biografia', 2011, 'PROLONGED',
 'Oficjalna biografia założyciela Apple.'),

('Sapiens', 'Yuval Noah Harari', '9788308064139', 'Nauka', 2014, 'PROLONGED',
 'Opowieść o historii gatunku ludzkiego.'),

('Hobbit', 'J.R.R. Tolkien', '9788324403752', 'Fantasy', 1937, 'AVAILABLE',
 'Przygody Bilbo Bagginsa w drodze do Samotnej Góry.');

INSERT INTO system_settings (id, max_loan_duration, user_loan_limit, daily_penalty_rate)
VALUES (1, 30, 5, 0.5);

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('admin', '$2a$10$Pw4d/82XBJQMnV7pEid3DOxfNVvgEMs./E3h5Nj7xDdyi4DtWEwti', 'System', 'Administrator', 'admin@biblioteka.pl', 'ADMIN');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('biblio', '$2a$10$lkqF2wdYpoBsu8jdjqxzne8g4AfuHAwkfybnRS1LOVahqdD9Oamum', 'Jan', 'Kowalski', 'j.kowalski@biblioteka.pl', 'LIBRARIAN');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('user', '$2a$10$HGyrSZI8stl2.RzU75hI3uOtcYfKBCudERDACRGY2pQsqHIKIkG/K', 'Anna', 'Nowak', 'a.nowak@poczta.pl', 'USER');

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'admin'), '123456789', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 8500.00);

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'biblio'), '987654321', TO_DATE('2023-03-15', 'YYYY-MM-DD'), 5200.50);

COMMIT;
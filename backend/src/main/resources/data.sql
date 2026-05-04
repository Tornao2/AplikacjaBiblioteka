DELETE FROM loans;
DELETE FROM staff_details;
DELETE FROM users;
DELETE FROM system_settings;
DELETE FROM books;
DELETE FROM system_logs;
DELETE FROM finance;

INSERT INTO system_logs (log_timestamp, username, action, details, severity)
VALUES
(TO_DATE('2026-04-08 10:15:30', 'YYYY-MM-DD HH24:MI:SS'), 'admin', 'LOGIN', 'Pomyślne logowanie do systemu', 'INFO');

INSERT INTO system_logs (log_timestamp, username, action, details, severity)
VALUES
(TO_DATE('2026-04-08 11:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'biblio', 'BOOK_ADD', 'Dodano nową książkę: Solaris (ISBN: 9788308061442)', 'INFO');

INSERT INTO system_logs (log_timestamp, username, action, details, severity)
VALUES
(TO_DATE('2026-04-08 12:45:12', 'YYYY-MM-DD HH24:MI:SS'), 'system', 'DB_ERROR', 'Błąd połączenia z bazą danych podczas próby odczytu', 'CRITICAL');

INSERT INTO system_logs (log_timestamp, username, action, details, severity)
VALUES
(TO_DATE('2026-04-08 14:20:05', 'YYYY-MM-DD HH24:MI:SS'), 'user', 'AUTH_FAIL', 'Nieudana próba logowania - błędne hasło', 'WARNING');

INSERT INTO system_logs (log_timestamp, username, action, details, severity)
VALUES
(TO_DATE('2026-04-08 15:05:00', 'YYYY-MM-DD HH24:MI:SS'), 'admin', 'SETTINGS_UPDATE', 'Zmiana maksymalnego czasu wypożyczenia z 30 na 45 dni', 'INFO');

INSERT INTO books (title, author, isbn, category, release_year, status, description)
VALUES
('Wiedźmin: Ostatnie życzenie', 'Andrzej Sapkowski', '9788375900989', 'Fantasy', 1993, 'Dostepna',
 'Zbiór opowiadań o wiedźminie Geralcie z Rivii.'),

('1984', 'George Orwell', '9788373926516', 'Dystopia', 1949, 'Dostepna',
 'Wizja totalitarnej przyszłości pod okiem Wielkiego Brata.'),

('Zbrodnia i kara', 'Fiodor Dostojewski', '9788377794357', 'Klasyka', 1866, 'Wypozyczona',
 'Psychologiczne studium morderstwa i sumienia.'),

('Marsjanin', 'Andy Weir', '9788328701045', 'Sci-Fi', 2011, 'Dostepna',
 'Historia astronauty, który musi przetrwać samotnie na Marsie.'),

('Steve Jobs', 'Walter Isaacson', '9788308048122', 'Biografia', 2011, 'Wypozyczona',
 'Oficjalna biografia założyciela Apple.'),

('Sapiens', 'Yuval Noah Harari', '9788308064139', 'Nauka', 2014, 'Dostepna',
 'Opowieść o historii gatunku ludzkiego.'),

('Hobbit', 'J.R.R. Tolkien', '9788324403752', 'Fantasy', 1937, 'Dostepna',
 'Przygody Bilbo Bagginsa w drodze do Samotnej Góry.');

INSERT INTO system_settings (id, max_loan_duration, user_loan_limit, daily_penalty_rate)
VALUES (1, 30, 5, 0.5);

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('biblio', '$2a$10$lkqF2wdYpoBsu8jdjqxzne8g4AfuHAwkfybnRS1LOVahqdD9Oamum', 'Jan', 'Kowalski', 'j.kowalski@biblioteka.pl', 'Bibliotekarz');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('admin', '$2a$10$Pw4d/82XBJQMnV7pEid3DOxfNVvgEMs./E3h5Nj7xDdyi4DtWEwti', 'System', 'Administrator', 'admin@biblioteka.pl', 'Admin');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('user', '$2a$10$HGyrSZI8stl2.RzU75hI3uOtcYfKBCudERDACRGY2pQsqHIKIkG/K', 'Anna', 'Nowak', 'a.nowak@poczta.pl', 'Czytelnik');

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'admin'), '123456789', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 8500.00);

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'biblio'), '987654321', TO_DATE('2023-03-15', 'YYYY-MM-DD'), 5200.50);

INSERT INTO loans (book_id, user_id, loan_date, due_date, return_date, extended, overdue_pay)
VALUES (
    (SELECT id FROM books WHERE title = 'Zbrodnia i kara'),
    (SELECT id FROM users WHERE username = 'admin'),
    TO_DATE('2026-04-15', 'YYYY-MM-DD'),
    TO_DATE('2026-05-15', 'YYYY-MM-DD'),
    NULL,
    0,
    0
);

INSERT INTO loans (book_id, user_id, loan_date, due_date, return_date, extended, overdue_pay)
VALUES (
    (SELECT id FROM books WHERE title = 'Steve Jobs'),
    (SELECT id FROM users WHERE username = 'user'),
    TO_DATE('2026-03-01', 'YYYY-MM-DD'),
    TO_DATE('2026-03-31', 'YYYY-MM-DD'),
    NULL,
    0,
    1700
);

INSERT INTO loans (book_id, user_id, loan_date, due_date, return_date, extended, overdue_pay)
VALUES (
    (SELECT id FROM books WHERE title = 'Sapiens'),
    (SELECT id FROM users WHERE username = 'user'),
    TO_DATE('2026-03-10', 'YYYY-MM-DD'),
    TO_DATE('2026-04-09', 'YYYY-MM-DD'),
    TO_DATE('2026-04-05', 'YYYY-MM-DD'),
    0,
    0
);

INSERT INTO loans (book_id, user_id, loan_date, due_date, return_date, extended, overdue_pay)
VALUES (
    (SELECT id FROM books WHERE title = 'Hobbit'),
    (SELECT id FROM users WHERE username = 'biblio'),
    TO_DATE('2026-04-01', 'YYYY-MM-DD'),
    TO_DATE('2026-05-08', 'YYYY-MM-DD'),
    NULL,
    1,
    0
);

INSERT INTO finance (transaction_date, type, amount, description)
VALUES (TO_DATE('2026-04-05', 'YYYY-MM-DD'), 'INCOME', 15.50, 'Opłata za przetrzymanie książki - user');

INSERT INTO finance (transaction_date, type, amount, description)
VALUES (TO_DATE('2026-04-10', 'YYYY-MM-DD'), 'EXPENSE', 120.00, 'Zakup nowych artykułów biurowych');

INSERT INTO finance (transaction_date, type, amount, description)
VALUES (TO_DATE('2026-04-20', 'YYYY-MM-DD'), 'INCOME', 500.00, 'Dotacja celowa na zakup nowości wydawniczych');

UPDATE books SET status = 'Wypozyczona' WHERE title = 'Hobbit';

COMMIT;
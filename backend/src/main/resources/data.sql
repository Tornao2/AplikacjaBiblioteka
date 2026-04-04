DELETE FROM staff_details;
DELETE FROM users;

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('admin', '$2a$10$Pw4d/82XBJQMnV7pEid3DOxfNVvgEMs./E3h5Nj7xDdyi4DtWEwti', 'System', 'Administrator', 'admin@biblioteka.pl', 'ADMIN');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('biblio', '$2a$10$lkqF2wdYpoBsu8jdjqxzne8g4AfuHAwkfybnRS1LOVahqdD9Oamum', 'Jan', 'Kowalski', 'j.kowalski@biblioteka.pl', 'LIBRARIAN');

INSERT INTO users (username, password, first_name, last_name, email, role)
VALUES ('user', '$2a$10$HGyrSZI8stl2.RzU75hI3uOtcYfKBCudERDACRGY2pQsqHIKIkG/K', 'Anna', 'Nowak', 'a.nowak@poczta.pl', 'USER');

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'admin'), '123-456-789', TO_DATE('2020-01-01', 'YYYY-MM-DD'), 8500.00);

INSERT INTO staff_details (user_id, phone_number, hire_date, salary)
VALUES ((SELECT id FROM users WHERE username = 'biblio'), '987-654-321', TO_DATE('2023-03-15', 'YYYY-MM-DD'), 5200.50);

COMMIT;
CREATE USER admin WITH PASSWORD 'admin';
CREATE DATABASE ws1_db;
GRANT ALL PRIVILEGES ON DATABASE ws1_db TO admin;
\c ws1_db admin;
CREATE TABLE users
(
  id            BIGSERIAL PRIMARY KEY,
  login         VARCHAR(255),
  password      VARCHAR(255),
  email         VARCHAR(255),
  gender        BOOLEAN,
  register_date   DATE
);


INSERT INTO users (login, password, email, gender, register_date)
VALUES ('izard', '1234', 'a1@tune-it.neru', true, '1996.01.01');
INSERT INTO users (login, password, email, gender, register_date)
VALUES ('adpash', '1234', 'a2@tune-it.neru', true, '1996.02.02');
INSERT INTO users (login, password, email, gender, register_date)
VALUES ('ivanusk', 'qwerty', 'iu@tune-it.neru', true, '1996.03.03');

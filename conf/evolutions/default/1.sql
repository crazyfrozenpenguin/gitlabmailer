# --- First database schema

# --- !Ups

CREATE TABLE account (
  email                     VARCHAR(50) PRIMARY KEY,
  projects                  VARCHAR(1000) NOT NULL
);

INSERT INTO account(email, projects) VALUES ('foo@gmail.com','gitlabmailer,proj1,proj2');
INSERT INTO account(email, projects) VALUES ('bar@gmail.com','gitlabmailer,proj1');

# --- !Downs

DROP TABLE IF EXISTS account;
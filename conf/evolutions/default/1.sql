# --- First database schema

# --- !Ups

CREATE TABLE account (
  email                     VARCHAR(50) PRIMARY KEY,
  projects                  VARCHAR(1000),
  format                    VARCHAR(4) NOT NULL
);

INSERT INTO account(email, projects, format) VALUES ('foo@gmail.com','gitlabmailer,proj1,proj2', 'Text');
INSERT INTO account(email, projects, format) VALUES ('bar@gmail.com','gitlabmailer,proj1', 'HTML');

# --- !Downs

DROP TABLE IF EXISTS account;
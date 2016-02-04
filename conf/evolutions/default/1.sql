# Users schema

# --- !Ups

CREATE TABLE User (
    id bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    UNIQUE (email)
);

# --- !Downs

DROP TABLE User;
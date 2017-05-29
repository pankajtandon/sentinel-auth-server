-- User tables

CREATE TABLE IF NOT EXISTS user (
    id BIGINT(20) not null auto_increment,
    username VARCHAR(255) not null unique,
    password VARCHAR(255) not null,
    enabled TINYINT(1),
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS authorities (
    id BIGINT(20) not null auto_increment,
    username VARCHAR(255) not null,
    authority VARCHAR(255) not null,
    PRIMARY KEY (id)
);
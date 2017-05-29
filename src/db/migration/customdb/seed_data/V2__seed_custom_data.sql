INSERT INTO user (username, password, enabled) VALUES ('admin', 'password', true);
INSERT INTO user (username, password, enabled) VALUES ('joe', 'password', true);
INSERT INTO user (username, password, enabled) VALUES ('doe', 'password', true);

INSERT INTO authorities (username, authority) VALUES ('admin', 'MANAGE_USER');
INSERT INTO authorities (username, authority) VALUES ('admin', 'DISABLE_USER');
INSERT INTO authorities (username, authority) VALUES ('admin', 'DELETE_USER');
INSERT INTO authorities (username, authority) VALUES ('admin', 'MANAGE_ROLE');
INSERT INTO authorities (username, authority) VALUES ('admin', 'DELETE_ROLE');
INSERT INTO authorities (username, authority) VALUES ('admin', 'MANAGE_PET');
INSERT INTO authorities (username, authority) VALUES ('admin', 'DELETE_PET');
INSERT INTO authorities (username, authority) VALUES ('admin', 'MANAGE_INVOICE');
INSERT INTO authorities (username, authority) VALUES ('admin', 'DELETE_INVOICE');

INSERT INTO authorities (username, authority) VALUES ('joe', 'MANAGE_PET');
INSERT INTO authorities (username, authority) VALUES ('joe', 'DELETE_PET');
INSERT INTO authorities (username, authority) VALUES ('joe', 'MANAGE_INVOICE');
INSERT INTO authorities (username, authority) VALUES ('joe', 'DELETE_INVOICE');

INSERT INTO authorities (username, authority) VALUES ('doe', 'MANAGE_PET');
INSERT INTO authorities (username, authority) VALUES ('doe', 'MANAGE_INVOICE');
Sentinel Auth Server
===

This is a Spring based OAuth2 Authorization Server implementation,
backed by a RDBMS.

This implementation can be used to:

* Produce an encrypted JWT token that contains the authorities that the
authenticated user possesses.
*  Request a JWT token based on the [Password Credentials](https://auth0.com/docs/api-auth/which-oauth-flow-to-use) grant.

The datasources needed for the OAuth db and the db that holds the user
and role/authorities values can be configured via external config.


### Datasource preperation:

#### DDL:
Create a db called __oauth2db__ (say) and populate it using the following DDL and DML:

```
drop table if exists oauth_client_details;
create table oauth_client_details (
  client_id VARCHAR(255) PRIMARY KEY,
  resource_ids VARCHAR(255),
  client_secret VARCHAR(255),
  scope VARCHAR(255),
  authorized_grant_types VARCHAR(255),
  web_server_redirect_uri VARCHAR(255),
  authorities VARCHAR(255),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(255)
);

-- Change the client_id and client_secret to suit your org.

INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	refresh_token_validity, additional_information, autoapprove)
VALUES
	("acme", "some_secret", "read,write",
	"password,refresh_token", null, null, 36000, 36000, null, true);
```


Similarly create another db (or use an existing db) where user info will need to be stored.
Let's call it _userDb_

If the _user_ and _authorities_ tables don't already exist, then please run the following DDL:

```
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
```


The above step of creating DDL and data can be achieved by
running the following command also.
However, the db properties will need to be set in the
flyway maven plugin config in the pom of the project.

```
mvn flyway:migrate@oauthdb

and

mvn flyway:migrate@customdb

```

### Configuration

#### Custom Property File
Create a custom yaml file with the following properties

```
server:
  # This is the name following the server:port
  contextPath: /sentinel-oauth2-server
  # The port on which the AuthServer will listen
  port: 8081

# Details about the database server where the oauth DDL and DML was run:
oauthdb:
  jdbc:
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/oauth2db
    user: oauth2user
    pass: password

# Details about the database server User info lives:
userdb:
  jdbc:
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/customdb
    user: customuser
    pass: password


# Note the below queries can be any queries so long as the data in the
# SELECT list and WHERE clause is retained.
query:
    # This query returns the username and password, given a username
    usersByUsername: SELECT username, password, enabled FROM user WHERE username=?

    # This query returns username and authorities, given a username
    authoritiesByUsername: SELECT username, authority FROM authorities WHERE username=?

jwt:
  accessTokenValidityInSeconds: 36000
  refreshTokenValidityInSeconds: 360000
```



#### Build the project

```
mvn clean install
```

#### Run the Auth Server

```
java -jar target/sentinel-oauth2-server.war --spring.config.location=/absolute/path/to/custom.yaml
```

#### Generate a JWT token using POSTMAN

```
POST localhost:8081/sentinel-oauth2-server/oauth/token?grant_type=password&username=admin&password=password
```
The access-token returned in the response can be examined on the [JWT](jwt.io) site

Change the username/password to match the values specified in the above INSERT statements.



## Resource Server

The Authorization Server produces encrypted JWT tokens using a key store
 as shown below:

#### Generate Key Pair

```$xslt
keytool -genkeypair -alias sentinel
                    -keyalg RSA
                    -keypass <shh-super-secret>
                    -keystore sentinel.jks
                    -storepass <shh-super-secret>
```

#### Export Public Key

```$xslt
keytool -list -rfc --keystore sentinel.jks | openssl x509 -inform pem -pubkey
```


#### Public key for Sentinel Oauth2

Use this public key on the ResourceServer to process the JWT token produced by the sentinel-oauth2-server:

```$xslt
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhz1VktJlpELfFo9Qcyy0
e6EPPJnRVOmVVjRJDFpWqgJty4TTW55uuUP0vOu+sBr4Bn9BTGcwBXKlZ3IyJ4mW
XbBzA4JXAbM0MaO4QyMz2encYqAoyaZNtrvfuCIk6mQjxk7B27SvgnzB2NPoLX9D
t3yGRDo5JUCQkNZ870iO/1nZh6mZcnT/7WyRtbfkFXWmkTkiOJOTAtLI5rZidTOG
i4VVEutZ7yQiUkhsnvHreRYdQCAhbW3shkFzWl0OG0ZgABhxfJ3Sdp6FGHV+iW2L
0ykCiF3KSh4MV8tvMGd/qx4TF4gmebyRvL4NF6jmkP15O++4FlblrLrP6DKJJVVC
UwIDAQAB
-----END PUBLIC KEY-----
```


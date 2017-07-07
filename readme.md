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

-- To use permissions
CREATE TABLE IF NOT EXISTS authorities (
    id BIGINT(20) not null auto_increment,
    username VARCHAR(255) not null,
    authority VARCHAR(255) not null,
    PRIMARY KEY (id)
);

-- To use roles
CREATE TABLE `role` (
  `id` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL,
  `role_id` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `role_permission` (
  `role_id` int(11) NOT NULL,
  `permission` varchar(45) NOT NULL,
  PRIMARY KEY (`role_id`,`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

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
  contextPath: /sentinel-auth-server
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
    # Sample:
    # authoritiesByUsername: SELECT username, authority FROM authorities WHERE username=?
    #
    # Note that the first element in this SELECT list below is never used by Spring and can therefore be 'dummied-out'
    # But it needs to exist so that Spring can access the second argument at index 2.
    authoritiesByUsername: |
                 SELECT DISTINCT 'username', permission FROM role_permission rp
                 WHERE rp.role_id IN
                       (SELECT role.id
                        FROM user
                        INNER JOIN user_role ON user.user_id = user_role.user_id
                        INNER JOIN role role ON user_role.role_id = role.id WHERE user.user_name = ? )
                        
                        
    # This query can be used to return ANY data from the customdb so long as the following rules are followed:
    # The SELECT list should contain at least two columns labeled 'key' and 'value'. If other elements exist in
    # the SELECT list *after* these two, they will be ignored.
    # The WHERE clause MUST be as shown below. The username must be supplied by the caller of the query.    
    additionalInfoQuery: |
          SELECT 'role' AS 'key' , role.name AS 'value'
          FROM user
          INNER JOIN user_role ON user.user_id = user_role.user_id
          INNER JOIN role role ON user_role.role_id = role.id WHERE user.user_name = ?
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
java -jar target/sentinel-auth-server.war --spring.config.location=/absolute/path/to/custom.yaml
```

#### Generate a JWT token using POSTMAN

```
POST localhost:8081/sentinel-auth-server/oauth/token?grant_type=password&username=admin&password=password
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

Use this public key on the ResourceServer to process the JWT token produced by the sentinel-auth-server:

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


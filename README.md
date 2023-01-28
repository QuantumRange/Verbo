# Verbo (**Still in development**)

A vocabulary learning platform.

## Docker

You can use verbo via docker-compose, here is one example:

```yaml
version: '3.7'
services:
  verbo:
    image: qrqrqr/verbo:latest
    ports:
      - "80:80"
    environment:
      - spring.datasource.url=jdbc:mariadb://localhost:3306/verbo
      - spring.datasource.username=root
      - spring.datasource.password=root
      - spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

To configure the Config, set the environment variables as shown.

## Config

The configuration for my Verbo application can be set up in several ways, but the three most common methods are

- You can pass config values as command line arguments when starting the program. For example, if you want to set the
  value of a property called "apiKey" to "abc123", you can pass the argument "-DapiKey=abc123" when starting the
  program.
- You can create a file called "application.properties" or "application.yml" in the same location as the .jar and set
  configuration values for the program. These values will be used as the default configuration for the application if no
  other configuration values are specified.

For more information about configuring the application, refer to the Spring Boot
documentation: [24. Externalised Configuration](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html)

### SQL Database

The table below lists all supported SQL databases. If you don't see yours in the list, create a new issue.

| SQL Implementation   | `spring.datasource.url`                     | `spring.datasource.driver-class-name`          |
|----------------------|---------------------------------------------|------------------------------------------------|
| MySQL                | `jdbc:mysql://<url>:<port>/<database>`      | `com.mysql.cj.jdbc.Driver`                     |
| MariaDB              | `jdbc:mariadb://<url>:<port>/<database>`    | `org.mariadb.jdbc.Driver`                      |
| PostgreSQL           | `jdbc:postgresql://<url>:<port>/<database>` | `org.postgresql.Driver`                        |
| Microsoft SQL Server | `jdbc:sqlserver://<url>:<port>/<database>`  | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| SQLite               | `jdbc:sqlite://<url>:<port>/<database>`     | `org.sqlite.JDBC`                              |
| HSQLDB               | `jdbc:hsqldb://<url>:<port>/<database>`     | `org.hsqldb.jdbc.JDBCDriver`                   |

Example database configuration for MariaDB:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/verbo
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

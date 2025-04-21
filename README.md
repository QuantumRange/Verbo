# !! This project is no longer maintained !!

# Verbo
**This repository is inactive, but I will fix bugs or continue development if there is interest.**
A vocabulary learning platform.

## Features
- [x] Learning vocabulary 
- [x] Learning groups
- [ ] Live Mode (Competition)
- [ ] Group right system

## Screenshots
Here are all the groups that the student has access to. Teachers can see all courses and create new ones.
![image](https://github.com/QuantumRange/Verbo/assets/49843948/55f676f5-f3d1-4c07-98d5-f9c004231504) <br>
They are also quickly accessible via a drop-down menu. <br>
![image](https://github.com/QuantumRange/Verbo/assets/49843948/3d035a38-79ec-43b6-a326-00e8740388ad) <br>
A group can contain multable sets, including sets that are in different groups. 
![image](https://github.com/QuantumRange/Verbo/assets/49843948/7520d4ee-f0af-4c88-b7c4-d943d594ebf4) <br>
Teachers can set information for tests and exams, with directly selectable vocabulary.
![image](https://github.com/QuantumRange/Verbo/assets/49843948/4d2ec9e7-1740-4748-985c-ef5008722ca3) <br>
Each user has a personalised view of a set, they see witch words they know they want with which direction.
They can then choose a learning mode, for the go 'Card' is great because you don't need to type and at home 'Text'. But 'Full' automatically gives you cards and then text, depending on how well you know the word.

![image](https://github.com/QuantumRange/Verbo/assets/49843948/45c45372-78c5-43e3-88dd-2c1c84702c82) <br>
Here are a few screenshots for the 'Card' mode:
![image](https://github.com/QuantumRange/Verbo/assets/49843948/d77bc4f6-4d45-4231-9d43-7223cef7d6c4) <br>
![image](https://github.com/QuantumRange/Verbo/assets/49843948/4e1392a8-458c-476a-bf1f-43ea1979a8fe) <br>
![image](https://github.com/QuantumRange/Verbo/assets/49843948/93b65084-475f-4a92-87d5-0aabfe4369bc) <br>

Here are a few screenshots for the 'Text' mode:
![image](https://github.com/QuantumRange/Verbo/assets/49843948/47ff424e-9c55-468d-8205-321beafa152f) <br>
![image](https://github.com/QuantumRange/Verbo/assets/49843948/a5a5ae8f-5e62-4938-9723-573f16b8a110) <br>

The user can customise the learning algorithm.
So if you want to type the word correctly every time and then move on, you can tweak the sliders.
![image](https://github.com/QuantumRange/Verbo/assets/49843948/6edb8541-b098-48a1-8000-6e8b896aef9f) <br>

For administrators and teachers there is also an admin panel with all users and the ability to change their rank and password (the system generates a random password for the user and then forces them to change it on their first login) and delete the account.
![image](https://github.com/QuantumRange/Verbo/assets/49843948/551507e4-362c-4f3e-8f42-3c8f7906df7f) <br>

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
The default admin user is `root`:`root`.

### MariaDB Quickstart
Here is a quick start docker compose file for a mariadb setup for Verbo.

```yaml
version: '3.7'
services:
  mariadb:
    image: mariadb:latest
    restart: always
    environment:
      - MYSQL_ROOT_PASSWORD=my_root_password
      - MYSQL_DATABASE=verbo
      - MYSQL_USER=my_user
      - MYSQL_PASSWORD=my_password
    volumes:
      - verbo_data:/var/lib/mysql

  verbo:
    image: qrqrqr/verbo:latest
    depends_on:
      - mariadb
    ports:
      - "80:8080"
    environment:
      - spring.datasource.url=jdbc:mariadb://mariadb:3306/verbo
      - spring.datasource.username=my_user
      - spring.datasource.password=my_password
      - spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

volumes:
  verbo_data:
```

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

Btw. if you want to change the port because you don't run this in a docker container you can add `server.port=<port>`.

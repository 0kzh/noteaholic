# Server

## How to run

1. Create a `database.conf` file in `server/src/main/resources/`

* Add the configurations for the file (see below for test version):

    ```shell
    $ cat server/src/main/resources/database.conf

    database {
        connectionString = "jdbc:pgsql://localhost:5432/postgres"
        driver = "com.impossibl.postgres.jdbc.PGDriver"
        username = "postgres"
        password = "postgres"
    }
    ```
* Create a `jwt.conf` file in `server/src/main/resources` and add configurations for the file. Below is an example of a
  configuration for such a file:

   ```shell
   $ cat /server/src/main/resources/jwt.conf
  
   jwt {
      secret = "secret"
      issuer = "http://0.0.0.0:8080/"
      audience = "http://0.0.0.0:8080/login"
      realm = "authentication"
   }
   ```

* Run the server with `idea` or with `gradle`
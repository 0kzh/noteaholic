# Server

## How to run

1. Create a `database.conf` file in `server/src/main/resources/`
2. Add the configurations for the file (see below for test version):

    ```shell
    $ cat server/src/main/resources/database.conf

    database {
        connectionString = "jdbc:pgsql://localhost:5432/postgres"
        driver = "com.impossibl.postgres.jdbc.PGDriver"
        username = "postgres"
        password = "postgres"
    }
    ```

3. Run the server with `idea` or with `gradle`
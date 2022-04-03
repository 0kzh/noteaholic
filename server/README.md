[[_TOC_]]

## About

In order for our note taking application to function, an API is required. This server provides an API for note
management, searching, authentication and access control. It is built using Ktor using the coroutine-based IO
engine ([CIO](https://ktor.io/docs/engines.html)). The server implements the REST protocol.

## Getting Started

1. Create a `database.conf` file in `server/src/main/resources/`

* Add the configuration options of the database into the file (see below for test version):

    ```shell
    $ cat server/src/main/resources/database.conf

    database {
        connectionString = "jdbc:pgsql://localhost:5432/postgres"
        driver = "com.impossibl.postgres.jdbc.PGDriver"
        username = "postgres"
        password = "postgres"
    }
    ```

2. Create a `jwt.conf` file in `server/src/main/resources` and add configurations into the file. Below is an example of
   a configuration for such a file:

   ```shell
   $ cat /server/src/main/resources/jwt.conf
  
   jwt {
      secret = "secret"
      issuer = "http://0.0.0.0:8080/"
      audience = "http://0.0.0.0:8080/login"
      realm = "authentication"
   }
   ```

3. Create a `sendgrid.conf` file in `server/src/main/resources` and add configurations into the file. Below is an
   example of a configuration for such a file:

    ```shell
    $ cat /server/src/main/resources/sendgrid.conf

    sendgrid {
      key = "xx.xx.xx"
    }
    ```

4. Run the server with your IDE or with `gradlew run`

## Endpoints

All endpoints expect JSON and return JSON so ensure the `Content-Type: application/json` header is added to your
requests. Additionally, for authenticated routes ensure that the `Authorization: Bearer=<TOKEN>` header is added.

Additionally, if the request is not successful the HTTP status code will not be 200. The response will follow the
following format:

```json
{
  "error": "MESSAGE_CODE",
  "errorMessage": "Message Error Message"
}
```

The possible errors are:

| `error`         | `status_code` | Description                       |
|-----------------|---------------|-----------------------------------|
| `ERR_EXISTS`    | `409`         | The record already exists         |
| `ERR_LENGTH`    | `400`         | Length constraint violated        |
| `ERR_EMPTY`     | `400`         | Unexpected empty data             |
| `ERR_NOT_FOUND` | `404`         | Could not find the record         |
| `ERR_MALFORMED` | `400`         | Data provided is invalid          |
| `ERR_ACCESS`    | `400`         | Access not resource is restricted |
| `ERR_PASSWORD`  | `400`         | Password constraints violated     |


### Authentication

#### Login

POST http://localhost/login

**Request:**

```json
{
  "email": "test@test.com",
  "password": "abcDEF$123"
}
```

**Response:**

```json
{
  // token redacted
  "token": "xxxx.xxxxx.xxxx"
}
```

#### Signup

POST http://localhost/signup

**Request:**

```json
{
  "firstName": "First Name",
  "lastName": "Last Name",
  "email": "test@test.com",
  "password": "abcDEF$123"
}
```

**Response:**

```json
{
  "id": 20,
  "firstName": "First Name",
  "lastName": "Last Name",
  "email": "test@test.ca",
  "lastSignInDate": "2022-04-02T22:23:26.618466"
}
```

### Note Operations

#### Search

GET http://localhost/search

**Request:**

This request uses a query parameter `?query=<SEARCH>`

See the wiki for supported search queries

**Response:**

```json
[
  {
    "note": {
      "id": 5,
      "title": "AbcDef",
      "positionX": 118,
      "positionY": 200,
      "plainTextContent": "# abc\n\n\ncats dogs fish horses \n\nmore cool stuff coming soon!",
      "formattedContent": "# abc\n\n\ncats dogs fish horses \n\nmore cool stuff coming soon!",
      "colour": "-4551713",
      "createdAt": "2022-03-25T02:46:32.109117",
      "modifiedAt": "2022-03-25T04:00:41.156535",
      "ownerID": 14
    },
    "matchingBody": "# <b>abc</b>\n\n\ncats dogs fish horses \n\nmore cool stuff coming soon!"
  }
]
```

### Create a Note

POST http://localhost/note

**Request:**

```json
{
  "title": "Title",
  "plainTextContent": "Content",
  "formattedContent": "# Content",
  "positionX": 111,
  "positionY": 129,
  // Colour is a long which represents a rgba color value
  "colour": "0"
}
```

**Response:**

```json
{
  "id": 37,
  "title": "Title",
  "positionX": 111,
  "positionY": 129,
  "plainTextContent": "Content",
  "formattedContent": "# Content",
  "colour": "0",
  "createdAt": "2022-04-02T22:45:03.458428",
  "modifiedAt": "2022-04-02T22:45:03.458481",
  "ownerID": 14
}
```

### Get User's Notes

AUTHENTICATION REQUIRED

GET http://localhost/notes

**Response:**

```json
[
  {
    "id": 5,
    "title": "Title",
    "positionX": 118,
    "positionY": 200,
    "plainTextContent": "Example\nHello World",
    "formattedContent": "# Example\nHello World",
    "colour": "-4551713",
    "createdAt": "2022-03-25T02:46:32.109117",
    "modifiedAt": "2022-03-25T04:00:41.156535",
    "ownerID": 14
  }
]
```

### Get a Note by id

AUTHENTICATION REQUIRED

GET http://localhost/note/{{id}}

**Response:**

```json
{
  "id": 5,
  "title": "Title",
  "positionX": 118,
  "positionY": 200,
  "plainTextContent": "Example\nHello World",
  "formattedContent": "# Example\nHello World",
  "colour": "-4551713",
  "createdAt": "2022-03-25T02:46:32.109117",
  "modifiedAt": "2022-03-25T04:00:41.156535",
  "ownerID": 14
}
```

### Update a Note by id

AUTHENTICATION REQUIRED

PATCH http://localhost/note/{{id}}

This endpoint supports partial note updates so only provide fields that should be updated.

**Request:**

```json
{
  // optional
  "title": "New Title",
  // optional
  "positionX": 118,
  // optional
  "positionY": 200,
  // optional
  "plainTextContent": "Example\nHello World",
  // optional
  "formattedContent": "# Example\nHello World",
  // optional
  "colour": "-4551713",
  // optional
  "ownerID": 14
}
```

**Response:**

```json
{
  "id": 5,
  "title": "New Title",
  "positionX": 118,
  "positionY": 200,
  "plainTextContent": "Example\nHello World",
  "formattedContent": "# Example\nHello World",
  "colour": "-4551713",
  "createdAt": "2022-03-25T02:46:32.109117",
  "modifiedAt": "2022-03-25T04:00:41.156535",
  "ownerID": 14
}
```

### Delete a Note by id

AUTHENTICATION REQUIRED

DELETE http://localhost/note/{{id}}

**Response:**

200 status code for successful deletion

### Share a Note

AUTHENTICATION REQUIRED

POST http://localhost/note/add_collaborator

**Request:**

```json
{
  "noteID": 5,
  "userEmails": [
    "leon.fattakhov@uwaterloo.ca"
  ]
}
```

**Response:**

```json5
[
  {
    "id": 9,
    "noteID": 5,
    "userID": 19
  }
]
```






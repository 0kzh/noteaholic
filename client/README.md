# Client Configuration

The client determines which server to connect to based on the configuration in the
`resources/config/config.json`

As a result this file needs to be created it if it doesn't exist and populated with a url. The following is an example
configuration that can be used for testing.

```json
{
  "url": "http://localhost:8082"
}
```

After setting up the configuration file the client can be started with `gradlew run`

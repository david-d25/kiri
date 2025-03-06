# Local configuration
To develop Kiri locally, create a file named `application-local.yml` here so you can override the default configuration.
This file is ignored by git.

## Examples
### Adding Telegram API key
```yaml
app.integration.telegram.apiKey: your-telegram-api-key
```

### Changing PostgreSQL database settings
```yaml
app:
  database:
    postgres:
      host: localhost
      port: 1234
      username: custom-username
      password: custom-password
```
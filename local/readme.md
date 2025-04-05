# Local configuration
To develop Kiri locally, create a file named `application-local.yml` here so you can override the default configuration.
The file is ignored by git.

## Examples
### Adding Telegram API key
```yaml
app.integration.telegram.apiKey: your-telegram-api-key
```

### Changing PostgreSQL database settings
```yaml
spring:
  datasource:
    url: custom-url
    username: custom-username
    password: custom-password
```
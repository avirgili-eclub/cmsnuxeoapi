
<center><a href="https://eclub.com.py/" target="blank"><img src="https://eclub.com.py/wp-content/uploads/2022/05/eclub-logo-en-color.svg" width="320" alt="eClub Logo" /></a></center>

# Content managment API for Nuxeo (CMS-Nuxeo-API)

## Descripción
API Wrapper de Nuxeo para manejar los documentos de los costumers.

## Documentación
Se implemento Swagger UI para ver los endpoints disponibles.

## Variables de entorno

Crear el archivo `.env` en la raíz del proyecto y agregar las siguientes configuraciones:

```dotenv
# Aplicación
APP_NAME=nuxeo-manager-service
APP_HOST=http://localhost
APP_PORT=8000
APP_SWAGGER_UI=true
ENVIRONMENT=development
MULTIPART_ENABLED=true
MAX_FILE=3MB
MAX_REQUEST=12MB
LEVEL_ROOT=INFO

# Nuxeo Server
NUXEO_API_URL=http://NUXEO_HOST:8080/nuxeo/api/v1
NUXEO_USER=usuario
NUXEO_PASS=clave

# Spring Boot Admin Server
ADMIN_SERVER_URL=http://127.0.0.1:8080
ADMIN_SERVER_USERNAME=usuario
ADMIN_SERVER_PASSWORD=clave

# Microservicio de usuarios
MICROSERVICE_USER_URL=http://user-microservice/
MICROSERVICE_USER_USERNAME=usuario
MICROSERVICE_USER_PASSWORD=clave

# Microservicio de notificaciones
MICROSERVICE_NOTIFICATION_URL=http://notification-microservice/
```

La variable `NUXEO_USER`, `NUXEO_PASS` debe ser una cuenta con rol de administrador en Nuxeo Server.

> **ATENCIÓN**: *se recomienda crear una cuenta con rol de administrador para utilizar en la app y no usar la cuenta por default de administrador.

## Docker

Para levantar todos los servicios del proyecto en Docker como ambiente de pruebas de forma local, se debe ejecutar el
comando `docker-compose up -d` o `docker compose up -d` según la versión con la que se esté trabajando.

Para levantar servicios de forma individual nada más, se debe ejecutar el comando `docker-compose up -d <servicio>`,
donde `<servicio>` corresponde al nombre del servicio que se desea levantar como figura en el
archivo `docker-compose.yml.example`.

[1]: https://typeorm.io/#/connection-options/common-connection-options

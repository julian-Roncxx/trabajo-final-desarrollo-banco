# Bckdbank

Aplicación bancaria hecha con Spring Boot, Spring Security y JWT para autenticación, gestión de cuentas y flujo de transacciones bancarias.

## Requisitos

- Java 17+
- Maven 3.9+
- MySQL 8+
- Postman o Thunder Client

## Ejecutar la aplicación

Desde la raíz del proyecto:

1. Abre una terminal.
2. Ejecuta:
   - Windows: `./mvnw spring-boot:run`
3. La API quedará disponible en:
   - `http://localhost:8080`

La aplicación usa MySQL. Por defecto se conecta a:

- Base de datos: `db_bank`
- Usuario: `root`
- Contraseña: *(vacía, como viene en XAMPP)*
- Servidor: `localhost:3306`

Puedes cambiar estos valores mediante las variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.

## Swagger / OpenAPI

Con la aplicación ejecutándose, abre la documentación interactiva en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:8080/v3/api-docs`

Para probar endpoints protegidos desde Swagger:

1. Ejecuta primero `POST /api/v1/auth/login`.
2. Copia el valor de `token` de la respuesta.
3. Pulsa **Authorize**.
4. Pega el token sin comillas ni el prefijo `Bearer`.
5. Pulsa **Authorize** y luego **Close**.

También puedes compilar y ejecutar con:

- `./mvnw clean package`
- `java -jar target/bckdbank-0.0.1-SNAPSHOT.jar`

## Cómo probarla con Postman o Thunder Client

No se usa curl ni terminal para probar la API. Todas las pruebas se hacen por endpoint desde la interfaz del cliente HTTP.

### 1. Registrar un usuario

En Postman o Thunder Client crea una petición de tipo POST.

- URL: `http://localhost:8080/api/v1/auth/register`
- Body: raw JSON

```json
{
  "firstName": "Ana",
  "lastName": "García",
  "email": "ana@correo.com",
  "password": "123456"
}
```

Haz clic en Send.

Si todo sale bien, la respuesta devolverá un token JWT y el email del usuario registrado.

### 2. Iniciar sesión

Crea otra petición POST.

- URL: `http://localhost:8080/api/v1/auth/login`
- Body: raw JSON

```json
{
  "email": "ana@correo.com",
  "password": "123456"
}
```

La respuesta deberá incluir el token JWT. Copia ese valor.

### 3. Usar el token para las rutas protegidas

Para cualquier endpoint protegido:

- Ve a la pestaña Headers
- Agrega esta cabecera:

```http
Authorization: Bearer <TOKEN_GENERADO>
```

Ejemplo:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4. Consultar cuentas del usuario autenticado

- Método: GET
- URL: `http://localhost:8080/api/v1/accounts`
- Headers:
  - `Authorization: Bearer <TOKEN_GENERADO>`

Haz clic en Send y revisa la respuesta.

### 5. Consultar una cuenta específica

- Método: GET
- URL: `http://localhost:8080/api/v1/accounts/{accountNumber}`
- Headers:
  - `Authorization: Bearer <TOKEN_GENERADO>`

Reemplaza `{accountNumber}` por el número devuelto al consultar las cuentas.

### 6. Registrar otro usuario

Para probar una transferencia, registra un segundo usuario y consulta su número de cuenta.

```json
{
  "firstName": "Luis",
  "lastName": "Pérez",
  "email": "luis@test.com",
  "password": "123456"
}
```

### 7. Realizar una transferencia

Inicia sesión con el usuario propietario de la cuenta de origen y usa su token.

- Método: POST
- URL: `http://localhost:8080/api/v1/transactions/transfer`
- Headers:
  - `Authorization: Bearer <TOKEN_GENERADO>`
  - `Content-Type: application/json`

```json
{
  "sourceAccountNumber": "CUENTA_ORIGEN",
  "destinationAccountNumber": "CUENTA_DESTINO",
  "amount": 250.00,
  "description": "Transferencia de prueba"
}
```

La cuenta de origen debe pertenecer al usuario autenticado y debe tener saldo suficiente.

### 8. Consultar el historial

- Método: GET
- URL: `http://localhost:8080/api/v1/transactions/account/{accountNumber}`
- Headers:
  - `Authorization: Bearer <TOKEN_GENERADO>`

El historial mostrará las transferencias asociadas a la cuenta indicada.

## Perfil y personalización

Cada usuario tiene un perfil personalizable: foto, datos personales, colores de la interfaz,
modo claro/oscuro, idioma, moneda, tamaño de letra, privacidad de saldos y notificaciones.
El perfil se crea automáticamente la primera vez que se consulta (también para usuarios antiguos).

### Interfaz web

Con la app corriendo abre **`http://localhost:8080`**: ahí puedes registrarte, iniciar sesión,
ver tus cuentas, transferir y revisar movimientos. Desde **Mi perfil** (`/perfil.html`) subes tu foto,
eliges un tema, cambias colores con vista previa en vivo, etc.

### Endpoints (todos requieren `Authorization: Bearer <TOKEN>`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/profile` | Perfil completo con preferencias |
| PATCH | `/api/v1/profile` | Actualiza datos personales (solo los campos enviados) |
| POST | `/api/v1/profile/avatar` | Sube la foto (`form-data`, campo `file`; JPG, PNG, WEBP o GIF, máx. 2 MB) |
| GET | `/api/v1/profile/avatar` | Descarga la foto |
| DELETE | `/api/v1/profile/avatar` | Elimina la foto (se muestran las iniciales) |
| GET | `/api/v1/profile/preferences` | Preferencias de interfaz |
| PATCH | `/api/v1/profile/preferences` | Cambia colores, modo, idioma, etc. (solo los campos enviados) |
| POST | `/api/v1/profile/preferences/reset` | Restablece las preferencias por defecto |
| GET | `/api/v1/profile/themes` | Temas de color predefinidos |
| PUT | `/api/v1/profile/password` | Cambia la contraseña |

#### Actualizar datos personales — `PATCH /api/v1/profile`

```json
{
  "displayName": "Anita",
  "phone": "+57 300 123 4567",
  "bio": "Ahorrando para mi viaje ✈️",
  "birthDate": "1998-04-21",
  "city": "Medellín",
  "country": "Colombia",
  "avatarColor": "#7C3AED"
}
```

Envía `""` en un campo de texto para borrarlo.

#### Subir foto — `POST /api/v1/profile/avatar`

En Postman: Body → **form-data** → clave `file`, tipo **File**, selecciona la imagen.
El tipo de imagen se valida por el contenido real del archivo, no por la extensión.

#### Cambiar apariencia — `PATCH /api/v1/profile/preferences`

```json
{
  "themeMode": "DARK",
  "primaryColor": "#7C3AED",
  "accentColor": "#06B6D4",
  "fontSize": "LARGE",
  "language": "ES",
  "currency": "COP",
  "compactMode": false,
  "hideBalances": true,
  "emailNotifications": true,
  "pushNotifications": false,
  "transactionAlerts": true
}
```

- `themeMode`: `LIGHT`, `DARK` o `SYSTEM`
- `fontSize`: `SMALL`, `MEDIUM` o `LARGE`
- `language`: `ES`, `EN` o `PT`
- `currency`: `COP`, `USD`, `EUR`, `MXN`, `PEN`, `CLP`, `ARS` o `BRL`
- Para aplicar un tema predefinido basta con `{ "themePreset": "grape" }`
  (ids: `ocean`, `forest`, `sunset`, `grape`, `ruby`, `graphite`, `coffee`, `mint`).

#### Cambiar contraseña — `PUT /api/v1/profile/password`

```json
{
  "currentPassword": "123456",
  "newPassword": "nuevaClave1",
  "confirmPassword": "nuevaClave1"
}
```

## Pruebas recomendadas por endpoint

### Registro

- POST `/api/v1/auth/register`
- Debe crear un usuario nuevo y asignarle una cuenta inicial.

### Login

- POST `/api/v1/auth/login`
- Debe devolver un token JWT válido.

### Cuentas

- GET `/api/v1/accounts`
- Debe devolver todas las cuentas del usuario actual.

- GET `/api/v1/accounts/{accountNumber}`
- Debe devolver los datos de una cuenta específica si pertenece al usuario autenticado.

### Transferencias

- POST `/api/v1/transactions/transfer`
- Debe descontar el monto de la cuenta de origen, sumarlo a la cuenta destino y registrar la transacción.

- GET `/api/v1/transactions/account/{accountNumber}`
- Debe devolver el historial de la cuenta autenticada.

## Datos de prueba sugeridos

Puedes crear varios usuarios para validar el flujo completo:

```json
{
  "firstName": "Luis",
  "lastName": "Pérez",
  "email": "luis@test.com",
  "password": "123456"
}
```

```json
{
  "firstName": "Maria",
  "lastName": "López",
  "email": "maria@test.com",
  "password": "123456"
}
```

## Solución de problemas

- Si aparece `401 Unauthorized`, revisa que el token esté bien enviado en el header `Authorization`.
- Si aparece `403 Forbidden`, verifica que Swagger o Postman esté usando el token correcto y que no tenga comillas. En Swagger, pega solo el token en **Authorize**.
- Si aparece `404 Not Found`, revisa que el número de cuenta exista y pertenezca al usuario autenticado.
- Si la app no arranca, revisa que Java 17 y MySQL estén instalados y ejecuta `./mvnw test` para verificar que el proyecto compila.

## Estructura principal del proyecto

```text
src/
  main/
    java/
      com/danidevbank/demo/
        config/
        controller/
        dto/
        entity/
        exception/
        repository/
        security/
        service/
  resources/
    application.yml
```

Los controladores principales son:

```text
controller/
  AuthController.java
  AccountController.java
  TransactionController.java
  ProfileController.java
```

## Tecnologías usadas

- Java 17
- Spring Boot 4.1.0
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Swagger / OpenAPI

## Autor

Proyecto desarrollado para práctica de banca digital con Java y Spring Boot.

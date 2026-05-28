# Accounts & Transactions Service

Servicio backend para la **gestión de cuentas y transacciones** de una institución financiera, expuesto vía API REST para múltiples canales. Implementa **arquitectura hexagonal**, autenticación **JWT (RS256)**, **idempotencia** en transacciones, **control de concurrencia** mediante bloqueo optimista, manejo de errores estandarizado (RFC 7807) y observabilidad (logs con correlación + métricas).

> Reto técnico — Especialista Back End (Arquitectura AWS).

---

## 1. Stack tecnológico

- **Java 21**, **Spring Boot 3.3**
- Spring Web, Spring Security, Spring Data JPA, Validation, Actuator
- **JWT RS256** (jjwt)
- **PostgreSQL** (perfil productivo) / **H2 en memoria** (perfil por defecto, para ejecución inmediata sin dependencias externas)
- **OpenAPI 3 / Swagger UI** (springdoc)
- **JaCoCo** (cobertura)
- **JUnit 5**, MockMvc, Testcontainers (opcional)
- **Docker / docker-compose**

---

## 2. Arquitectura hexagonal (Ports & Adapters)

La dependencia siempre apunta hacia el dominio. El dominio no conoce Spring ni JPA.

```
com.santander.accounts
├── domain                  # Núcleo: entidades, value objects e invariantes. Cero framework.
│   ├── model               # Account, Transaction, Money, Currency, TransactionType
│   └── exception           # Excepciones de negocio
├── application             # Casos de uso y puertos (contratos)
│   ├── port/in             # Puertos de entrada (casos de uso)
│   ├── port/out            # Puertos de salida (repositorios, idempotencia, eventos, token)
│   ├── service             # Orquestación de casos de uso (idempotencia, reintentos)
│   └── exception           # Errores de aplicación (conflicto, autenticación)
└── infrastructure          # Adaptadores concretos
    ├── adapter/in/web       # Controladores REST, DTOs, mappers, manejo de errores
    ├── adapter/out/persistence  # JPA: entidades, repos, adaptadores de los puertos
    ├── adapter/out/messaging    # Publicación de eventos (log local; SNS/SQS en AWS)
    ├── security             # JWT, filtros de autenticación y correlación
    └── config               # SecurityConfig, OpenAPI, propiedades
```

**Regla clave:** el dominio se valida a sí mismo (un débito sin saldo lanza excepción dentro de `Account`, no en el controller). Los DTOs viven en la capa web y nunca entran al dominio; las entidades JPA viven en persistencia y son distintas de las entidades de dominio.

---

## 3. Cómo ejecutar localmente

### Opción A — Ejecución inmediata con H2 (recomendada, sin Docker)

El perfil por defecto usa una base **H2 en memoria**, así que el servicio arranca sin instalar nada más.

- Desde IntelliJ: ejecuta la clase `AccountsTransactionsServiceApplication`.
- O por consola: `mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

La API queda en `http://localhost:8080`.

### Opción B — PostgreSQL con Docker

```bash
docker-compose up -d db          # levanta PostgreSQL
# Ejecutar la app con el perfil postgres:
mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

O para levantar todo (app + base) en contenedores:

```bash
docker-compose up --build
```

---

## 4. Endpoints

Todas las rutas requieren `Authorization: Bearer <token>`, excepto `POST /auth/login` y `GET /actuator/health`.

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/login` | Autentica y devuelve un JWT |
| POST | `/accounts` | Crea una cuenta (valida moneda, saldo ≥ 0, titular) |
| GET | `/accounts/{id}` | Detalle de una cuenta |
| GET | `/accounts?offset=&limit=` | Listado paginado (limit por defecto 20, máx. 100) |
| POST | `/transactions` | Crea transacción DEBIT/CREDIT. **Requiere header `Idempotency-Key`** |
| GET | `/accounts/{id}/transactions?from=&to=&type=` | Movimientos con filtros y paginación |
| GET | `/actuator/health` | Healthcheck |

**Documentación interactiva:** `http://localhost:8080/swagger-ui.html`
**Contrato OpenAPI:** `http://localhost:8080/v3/api-docs` (también incluido en `openapi.yaml`).

### Autenticación (usuario demo)

Credenciales por defecto (configurables en `application.yml`):

```
usuario: admin
clave:   admin123
```

```bash
# 1) Obtener token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2) Usar el token
curl http://localhost:8080/accounts \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 5. Decisiones técnicas clave

### Idempotencia en transacciones
`POST /transactions` exige el header `Idempotency-Key`. Se mantiene una tabla `idempotency_keys` con la clave (única), un hash del cuerpo de la petición, el estado y la referencia al resultado. Ante un reintento con la misma clave y el mismo cuerpo, **no se crea una segunda transacción**: se devuelve el resultado original. Si la clave se reutiliza con un cuerpo distinto, se responde `409 Conflict`. La unicidad de la clave a nivel de base de datos protege incluso ante reintentos concurrentes.

### Control de concurrencia (anti doble débito)
Las cuentas usan **bloqueo optimista** (`@Version`). Si dos débitos simultáneos intentan modificar la misma cuenta, solo uno persiste; el otro recibe un error de versión, se **reintenta** con el saldo actualizado y, si ya no hay fondos, falla con `409`/saldo insuficiente. Así el saldo nunca queda negativo. Está cubierto por una prueba de concurrencia específica.

### Manejo de errores
Respuestas de error consistentes basadas en **RFC 7807 (Problem Details)**, con `correlationId` para trazabilidad. Códigos: `201` creación, `400/422` validación, `401/403` autorización, `404` no encontrado, `409` conflicto/idempotencia, `500` error interno.

### Seguridad
JWT firmado con **RS256** (par de claves RSA), validando emisor, audiencia y expiración. Solo `/auth/login` y `/actuator/health` son públicos. Se añaden cabeceras de seguridad (`X-Content-Type-Options`, `X-Frame-Options`), CORS mínimo y **no se registran datos sensibles** (en logs solo identificadores). La clave de firma es inyectable por configuración (en AWS, vía Secrets Manager).

### Observabilidad
Cada petición recibe un `correlationId` (propagado en logs y devuelto como cabecera). Métricas vía Actuator/Micrometer (`/actuator/metrics`), incluyendo percentiles por endpoint.

---

## 6. Modelo de datos

- `accounts(id, owner, currency, balance, version, created_at)`
- `transactions(id, account_id, type, amount, currency, created_at)` — índice por `(account_id, created_at)`
- `idempotency_keys(idempotency_key, request_hash, status, result_ref, created_at)`

El DDL para PostgreSQL está en `db/schema.sql`.

---

## 7. Pruebas y cobertura

```bash
mvnw test            # ejecuta todas las pruebas (usan H2, no requieren Docker)
mvnw verify          # genera el reporte de cobertura JaCoCo
```

Incluye:
- Unitarias del dominio y casos de uso. Cobertura total ~74% (JaCoCo), con foco en el dominio (≈84%) y la lógica crítica.
- Prueba de **idempotencia** (misma clave dos veces → una sola transacción).
- Prueba de **concurrencia** (dos débitos en paralelo → el saldo nunca queda negativo).
- Pruebas de API con MockMvc.

Reporte de cobertura: `target/site/jacoco/index.html`.

---

## 8. Diseño para AWS

| Componente del reto | Servicio AWS | Rol |
|---------------------|--------------|-----|
| Entrada / enrutamiento | **API Gateway** | Throttling, validaciones básicas, rate limiting |
| Autenticación | **Cognito** (o JWT propio) | Validación de token (iss/aud/exp/kid) |
| Cómputo | **ECS Fargate** (o Lambda) | Ejecuta el servicio |
| Persistencia | **RDS PostgreSQL** | ACID, índices, bloqueo optimista |
| Idempotencia | RDS o **DynamoDB** (con TTL) | Almacén de claves |
| Eventos | **SNS/SQS** | Publicación de eventos de negocio |
| Secretos | **Secrets Manager** | Rotación de la clave de firma JWT |
| Observabilidad | **CloudWatch + X-Ray** | Logs, métricas y trazas distribuidas |

---

## 9. Alcance y priorización (plazo de 12 h)

Se priorizó el **núcleo funcional** (cuentas, transacciones, autenticación), la **arquitectura hexagonal** y los dos puntos de mayor riesgo del dominio: **idempotencia** y **control de concurrencia**, ambos cubiertos por pruebas.

Quedaron **documentados a nivel de diseño** (sección 8), por restricción de tiempo: IaC (Terraform/CloudFormation), CI/CD, despliegue real en AWS, pruebas de carga (k6) y el módulo opcional de tokenización de PAN.
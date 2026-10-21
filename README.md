# Customer Portal - Java Microservices

A customer self-service backend built as Spring Boot microservices behind an API gateway.

```
client -> api-gateway (JWT check) -> auth-service         (register, login, issues JWT)
                                  -> customer-service     (customer profiles)
                                  -> billing-service      (invoices) --Feign--> customer-service
                                  |                                  --Feign--> notification-service
                                  -> notification-service (in-app notifications)
all services register with discovery-server (Eureka); each service owns its own PostgreSQL database
```

## What it demonstrates
- API gateway with JWT validation; the gateway strips client identity headers and forwards `X-User-Id` / `X-User-Role`
- Service discovery (Eureka) and client-side load balancing (`lb://` routes, OpenFeign)
- Resilience4j circuit breaker + timeout on Feign calls; a fallback for best-effort notifications
- Database per service (Spring Data JPA, PostgreSQL), role-based access (CUSTOMER / ADMIN)
- Internal endpoints (`/internal/**`) that the gateway does not route
- One Dockerfile for every module, `docker compose up` for the whole system

## Run
Requires Docker. From this folder:

```bash
docker compose up --build
```

First build takes a few minutes. Eureka dashboard: http://localhost:8761. API: http://localhost:8080.

## Try it
```bash
# 1. register a customer and log in
curl -s -X POST localhost:8080/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"ada@example.com","password":"password123"}'
TOKEN=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ada@example.com","password":"password123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

# 2. create the customer profile (customer id = user id)
curl -s -X POST localhost:8080/customers/me -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"fullName":"Ada Lovelace","email":"ada@example.com","phone":"0123","address":"London"}'

# 3. log in as the seeded admin and issue an invoice to customer 2 (Ada's id; admin is id 1)
ADMIN=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@portal.local","password":"admin12345"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
curl -s -X POST localhost:8080/billing/invoices -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"customerId":2,"amount":49.99,"description":"October plan"}'

# 4. Ada sees and pays her invoice, then reads her notifications
curl -s localhost:8080/billing/invoices -H "Authorization: Bearer $TOKEN"
curl -s -X POST localhost:8080/billing/invoices/1/pay -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/notifications -H "Authorization: Bearer $TOKEN"
```
Check the user ids if yours differ: the first registered account after the admin seed gets the next id.

Resilience check: `docker compose stop notification-service`, pay an invoice, and the payment still succeeds
(the fallback logs and drops the notification). Stop `customer-service` and issuing an invoice returns 503.

## Build and test without Docker
Requires Java 21 and Maven 3.9+. Start PostgreSQL and the Eureka server yourself, or just run the unit tests:

```bash
mvn test
```

## Endpoints (through the gateway)
| Method | Path | Who |
|---|---|---|
| POST | /auth/register, /auth/login | public |
| POST/GET/PUT | /customers/me | customer |
| GET | /customers, /customers/{id} | admin |
| POST | /billing/invoices | admin |
| GET | /billing/invoices, /billing/invoices/{id} | customer (own) / admin (all) |
| POST | /billing/invoices/{id}/pay | invoice owner |
| GET | /notifications | customer |

## Notes
- Secrets in `docker-compose.yml` are demo defaults. Set `JWT_SECRET` (32+ characters) for anything real.
- Schema is created by Hibernate (`ddl-auto: update`). Use Flyway or Liquibase for production.

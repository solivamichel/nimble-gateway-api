# Nimble Gateway API

API **Nimble – Banking & Payments**: cadastro/login com JWT, criação e consulta de cobranças, pagamentos (saldo/cartão, com autorizador externo) e cancelamento com estorno.  
Documentação Swagger e execução via Docker/Compose.

> Requisitos do desafio (resumo): cadastro com senha segura, login por CPF ou e-mail, criação/consulta de cobranças (PENDING/PAID/CANCELED), pagamentos (saldo/cartão com autorizador), depósito com autorizador e **cancelamento** (pendente, pago por saldo com estorno e pago por cartão com autorização).

---

## 🚀 Stack
- Java 17, Spring Boot 3
- Spring Security (JWT), Spring Web + Validation, Spring Data JPA
- PostgreSQL
- WebClient (integração autorizador externo)
- Swagger / springdoc-openapi
- Gradle, Jacoco

---

## 🔧 Configuração

### Variáveis de ambiente
A API lê variáveis do ambiente (com defaults):
- `SPRING_DATASOURCE_URL` (ou use `SPRING_DATASOURCE_DB`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` com o compose)
- `JWT_SECRET` (obrigatório ser **forte**, 256 bits pra HS256)
- `JWT_EXPIRATION_MS` (padrão: `3600000`)

Exemplo `.env`:
```bash
SPRING_DATASOURCE_DB=nimble_db
SPRING_DATASOURCE_USERNAME=nimble_user
SPRING_DATASOURCE_PASSWORD=nimble_pass
JWT_SECRET=f6c7a5a8d1b24c0bb1d0a52e3aa9e82d58f3a7f9a1b2c3d4e5f6a7b8c9d0e1f2
JWT_EXPIRATION_MS=3600000
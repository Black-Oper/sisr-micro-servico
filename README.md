# sisr-microservico

Sistema de microsserviços poliglota para **Super-Resolução de Imagem (SISR)**, reaproveitando o modelo de deep learning (RTDVSR) do meu TCC. Projeto **build to learn**: o objetivo é aprender o padrão de mercado de ML servido em produção, não o TCC em si.

**Teste ao vivo:** https://sisr-micro-servico.vercel.app

## Arquitetura

- **frontend** — Next.js, hospedado na Vercel. Interface simples: envia uma imagem, acompanha o status e visualiza o resultado num slider antes/depois.
- **orchestrator** — Java / Spring Boot. Expõe a API REST, orquestra a fila, o storage e o estado dos jobs.
- **worker** — Python / PyTorch. Responsabilidade única: rodar o modelo de super-resolução.

A comunicação é assíncrona: o orchestrator publica o job numa fila (RabbitMQ) e responde na hora; o worker consome, processa e atualiza o status (Redis) e o resultado (MinIO).

```
Frontend → API (orchestrator) → fila (RabbitMQ) → Worker → modelo (PyTorch)
                ↓                                      ↓
              MinIO (imagens)                      Redis (status)
```

## Stack

| Camada | Tecnologia |
|---|---|
| Frontend | Next.js, TypeScript, Vitest |
| Orchestrator | Java 21, Spring Boot, JUnit, Testcontainers |
| Worker | Python, PyTorch, pytest |
| Fila | RabbitMQ |
| Estado | Redis |
| Storage | MinIO (S3-compatible) |
| Infra | Docker Compose, Caddy (HTTPS/TLS automático), AWS EC2, Vercel |
| CI/CD | GitHub Actions |

## Rodando localmente

```bash
cp .env.example .env
docker compose up -d --build
```

A API sobe em `http://localhost:8080`. O frontend roda separado:

```bash
cd frontend
npm install
npm run dev
```

## Metodologia

Projeto desenvolvido seguindo TDD (RED → GREEN) e com a arquitetura/contratos definidos antes da implementação, documentados no `CLAUDE.md`.

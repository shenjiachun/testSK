# testSK — Lightweight Automation Backend Service

A lightweight Java backend service for building automation products, powered by **Spring Boot** and **Claude AI** (Anthropic).

## Features

- **Automation Task Management** — Create, read, update, delete, and execute automation tasks via a RESTful API
- **Task Types**
  - `MANUAL` — Triggered on demand via `POST /api/tasks/{id}/execute`
  - `SCHEDULED` — Runs automatically on a cron schedule (e.g. `0 0 * * * *`)
  - `AI_ASSISTED` — Sends a configured prompt to Claude AI and stores the response
- **Dynamic Scheduler** — Register/cancel scheduled tasks at runtime without restart
- **Claude AI Integration** — Calls the Anthropic Messages API; configure via `ANTHROPIC_API_KEY`
- **H2 In-Memory Database** — Zero-config persistence for development; swap for any JPA-supported DB in production
- **Spring Boot Actuator** — Health, info, and metrics endpoints out of the box

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA + H2
- Spring Scheduling
- Anthropic Claude API (claude-3-5-haiku)
- Maven

## Quick Start

```bash
cd automation-service

# Build
mvn package -DskipTests

# Run (optionally set your Anthropic API key for AI tasks)
ANTHROPIC_API_KEY=sk-ant-... java -jar target/automation-service-0.0.1-SNAPSHOT.jar
```

The service starts on **http://localhost:8080**.

## API Reference

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tasks` | List all tasks (filter by `?status=` or `?type=`) |
| GET | `/api/tasks/{id}` | Get a single task |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |
| POST | `/api/tasks/{id}/execute` | Execute a task immediately |
| GET | `/api/tasks/{id}/scheduled` | Check if a task is currently scheduled |

### Example: Create an AI-assisted task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Daily summary",
    "type": "AI_ASSISTED",
    "aiPrompt": "Summarize the key highlights of today in bullet points."
  }'
```

### Example: Create a scheduled task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Hourly cleanup",
    "type": "SCHEDULED",
    "cronExpression": "0 0 * * * *"
  }'
```

## Configuration

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `anthropic.api-key` | `ANTHROPIC_API_KEY` | _(empty)_ | Anthropic API key |
| `anthropic.model` | — | `claude-3-5-haiku-20241022` | Claude model |
| `anthropic.max-tokens` | — | `1024` | Max tokens per response |
| `server.port` | — | `8080` | HTTP port |

## Running Tests

```bash
cd automation-service
mvn test
```

## H2 Console

Available at **http://localhost:8080/h2-console** (development only).
JDBC URL: `jdbc:h2:mem:automationdb`

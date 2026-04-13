# Zombieland // Yarmaq

**Zombieland** is a post-apocalyptic survival strategy integrated with the **Yarmaq** payment platform. Manage districts, survive the zombie outbreak, and handle your finances in a unified ecosystem.

## 🧟 Core Mechanics

- **District Management**: Claim and control city sectors. Each district has a **Survival Index** that affects its stability.
- **AI-Driven Sorties (Вылазки)**: Send your team on dangerous missions. Outcomes are dynamically generated using **Groq AI**, determining resource gains or losses based on your tactical descriptions.
- **Resource Economy**: Manage critical supplies like **Food**, **Ammo**, and **Materials** to keep your districts alive.
- **Financial Integration**: Use the built-in **Yarmaq** wallet to transfer KZT/YAR currencies between survivors.

## 🏗️ Architecture & Tech Stack

The project follows a modern microservices architecture with a polyglot approach:

### Services
| Service | Language | Description |
|---------|----------|-------------|
| **Zombie Manager** | Java (Spring Boot) | Core game logic, district management, and AI integration. |
| **Web Frontend** | Svelte (SvelteKit) | High-performance, cyberpunk-themed UI with Dark/Light mode support. |
| **Accounts Service** | Go | User balance and account management. |
| **Transaction Service** | Go | Saga-based reliable financial operations. |
| **Notification Service** | Go | Kafka-driven alerts for critical survival events. |

### Technologies
- **Backend**: Go 1.25+, Java 21+, Spring Boot 3.4+
- **Frontend**: Svelte 5, Tailwind 4, Lucide Icons
- **Infrastructure**: PostgreSQL, Kafka, Redis, Zitadel (OIDC/JWT)
- **Deployment**: Docker Compose, Traefik

## 📺 Preview
[![Смотреть видео demo](https://img.youtube.com/vi/IJdvXi-baeE/0.jpg)](https://youtu.be/IJdvXi-baeE)

## 🚀 Quick Start

1. **Prerequisites**: Docker, Docker Compose, Java 21 (for local dev), Go 1.25 (for local dev).
2. **Setup**:
   ```bash
   make up
   ```
3. **Access**:
   - Web UI: [http://localhost:5173](http://localhost:5173)
   - Zitadel: [http://zitadel.localhost:8080](http://zitadel.localhost:8080)

## 🗺️ Monorepo Structure
```
zombieland/
├── apps/
│   ├── zombie-manager/      # Java Service
│   ├── accounts-service/    # Go Service
│   ├── transaction-service/ # Go Service
│   └── notification-service/# Go Service
├── web/                     # Svelte Frontend
├── deployment/              # Docker configs
└── docs/                    # API documentation
```

---
*Built with survival in mind.*

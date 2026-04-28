# 🕵️ Detective Investigations

> Интерактивная платформа для ведения детективных расследований, с использованием искусственного интелекта для генерации кейсов и анализа гипотез.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-Integrated-blue.svg)](https://spring.io/projects/spring-ai)
[![OpenRouter](https://img.shields.io/badge/OpenRouter-API-purple.svg)](https://openrouter.ai/)

## 📋 Подробнее о проекте

Detective Investigations — это экспериментальная платформа для создания и ведения детективных дел: моделирование расследований, совместная работа пользователей, публикация улик по времени, live‑чат и базовый AI‑анализ гипотез. 

### ✨ Ключевые возможности

- 🗂️ AI‑генерация дел с кастомными настройками пользователя (Spring AI, OpenRouter API).
- 🧠 AI‑скоринг для вынесения решения на основе предположений пользователей.
- 🔁 Асинхронные сценарии и очереди событий через Apache Kafka.
- ⏰ Публикация улик по расписанию — симуляция поступления новой информации в дело.
- 💬 Realtime‑чат и нотификации через WebSocket для синхронной командной работы.
- 🔐 JWT‑аутентификация и разграничение прав доступа.

## 🚀 Стек технологий

### Backend
- Java 17
- Spring Boot 3
- Spring Security (JWT)
- Spring AI
- OpenRouter API (интеграция с LLM для генерации дел и анализа гипотез)

### Frontend
- Vue 3 + TypeScript + Vite

### Инфраструктура
- PostgreSQL
- Apache Kafka
- WebSocket
- Docker Compose

## 📦 Быстрый запуск (локально)

1) Поднять инфраструктуру (Postgres + Kafka):
```bash
cd backend
docker compose up -d
```

2) Запустить backend:
```bash
cd backend
./gradlew bootRun
```

3) Запустить frontend:
```bash
cd frontend
npm install
npm run dev
```
---

⭐️ Если проект вам понравился — поставьте звезду на GitHub!

**Made with ❤️ and ☕ in 2026**
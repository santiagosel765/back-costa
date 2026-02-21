# 🔐 ferrisys-auth

Authentication and authorization microservice for the **Ferrisys System**, a scalable and modular platform for hardware store management.

---

## 📌 Prerequisites

Ensure the following modules are already present and compiled:

### 📦 [ferrisys-common](../ferrisys-common)

> Contains shared entities (User, Role, Company, Module), DTOs, enums, and audit base classes.

### 📦 [ferrisys-parent](../ferrisys-parent)

> Parent POM for centralized dependency and plugin versions using Java 17 and Spring Boot 3.4.

---

## 🎯 Features

- 👤 **User Registration** with role and company assignment
- 🔐 **JWT-based login authentication**
- 🔄 **Password update/change**
- 🧩 **Role-based access** to system modules
- 🗂️ Uses centralized entities from `ferrisys-common`
- 📜 Loads default roles and module mappings from `data.sql`

---

## 📂 Project Structure

## ⚙️ Configuration

Set the required environment variables before running the service so credentials and secrets are not committed to the repository.

### Windows PowerShell

```powershell
Set-Item -Path Env:SPRING_DATASOURCE_URL -Value "jdbc:postgresql://HOST:5432/postgres?sslmode=require"
Set-Item -Path Env:SPRING_DATASOURCE_USERNAME -Value "postgres"
Set-Item -Path Env:SPRING_DATASOURCE_PASSWORD -Value "your_password"
Set-Item -Path Env:JWT_SECRET -Value "CHANGEME_32CHARS"
```

### Bash / Unix shells

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://HOST:5432/postgres?sslmode=require"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="your_password"
export JWT_SECRET="CHANGEME_32CHARS"
```

For local development you can copy [`application-local.yml.sample`](src/main/resources/application-local.yml.sample) to `application-local.yml` and adjust the values according to your environment.

---

## 🚀 Local development

The repository includes helper scripts that start the backend and frontend together so you can develop against both modules at the same time:

- **Bash / Unix shells**: run `../scripts/run-all.sh` from this directory (or `scripts/run-all.sh` from the repository root).
- **Windows PowerShell**: run `..\scripts\run-all.ps1` from this directory (or `scripts\run-all.ps1` from the repository root).

Both scripts install missing frontend dependencies, launch `./mvnw spring-boot:run` for this service, and start the Angular dev server. Use `Ctrl+C` to stop both processes.


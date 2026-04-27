# Electricity CMS (WAPDA)

A JavaFX desktop Complaint Management System for electricity consumers and staff. It supports role-based workflows for submitting, tracking, discussing, escalating, and resolving complaints.

## Key Features

- Role-based login and user context handling (customer and staff flows)
- Complaint submission with category/details capture
- Complaint list and filtering views (status/date-based dashboards)
- Complaint thread messaging with sender, role, timestamp, and message display
- Complaint lifecycle actions (including resolution and escalation flows)

## How to Run

### 1) Prerequisites

- Java 21
- PostgreSQL running and reachable
- Bash shell (commands below)

### 2) Configure environment variables

Create a `.env` file in the project root (or export OS env vars):

```env
DB_URL=jdbc:postgresql://localhost:5432/your_db
DB_USERNAME=your_user
DB_PASSWORD=your_password

# Optional DB behavior
DB_ALLOW_SCHEMA_UPDATE=false
DB_DDL_AUTO=none
DB_SHOW_SQL=false
DB_DISABLE_SERVER_PREPARED=true

# Optional SMTP (only needed if email is enabled)
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=your_smtp_user
SMTP_PASSWORD=your_smtp_password
SMTP_FROM=no-reply@example.com
```

### 3) Start the app

```bash
cd /home/gul/IdeaProjects/CMS-Wapda
./mvnw javafx:run
```

### 4) Useful commands

```bash
cd /home/gul/IdeaProjects/CMS-Wapda
./mvnw test
./mvnw -DskipTests compile
```

## Project Entry Point

- Main launcher: `com.electricity.cms.app.Launcher`
- JavaFX app: `com.electricity.cms.app.MainApp`


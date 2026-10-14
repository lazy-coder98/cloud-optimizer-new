# ☁️ Cloud Optimizer

**Full-stack cloud cost optimisation platform.** Enter a workload's CPU, memory and storage utilisation, and Cloud Optimizer flags over- or under-provisioned resources, estimates monthly savings, and asks Google Gemini for tailored advice. Every analysis is saved to your account history.

**🔗 Live demo:** https://cloud-optimizer-sjfn.onrender.com
<sub>Hosted on Render's free tier, so the first load can take up to a minute while the server wakes up.</sub>

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=flat-square&logo=react&logoColor=61DAFB)
![Gemini](https://img.shields.io/badge/Google_Gemini-8E75B2?style=flat-square&logo=googlegemini&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black)

---

## ✨ Features

- **Rule-based optimisation engine:** analyses CPU, memory and storage utilisation and recommends scaling up, rightsizing or downsizing, with a rationale for each suggestion.
- **Savings estimate:** calculates an estimated saving percentage (clamped between −35% and +65%) and a monthly saving amount from the current monthly cost.
- **Severity scoring:** classifies each result as `LOW`, `MEDIUM` or `HIGH` based on scaling risk, optimisation opportunity and spend.
- **Provider-aware advice:** tailored tips for **AWS** (S3 lifecycle rules, Savings Plans), **Azure** (Blob cool/archive tiers, Reserved VM Instances) and **GCP** (Nearline/Archive, committed use discounts).
- **Workload-aware tuning:** adjusts recommendations for production, batch, dev/test and database workloads.
- **AI cost advisor:** sends the analysis to **Google Gemini** (default `gemini-2.5-flash`) for natural-language advice and follow-up questions.
- **Secure accounts:** signup/login with **BCrypt**-hashed passwords and stateless **JWT** authentication; all optimisation and AI endpoints require a token.
- **Per-user history:** every analysis is stored in MongoDB and served back with pagination.
- **Single deployable:** the React frontend is built by Maven (`frontend-maven-plugin`) and served by Spring Boot, so one JAR and one Docker image hold the whole app.

## 🏗️ Architecture

```
React 19 SPA ──► Spring Boot REST API ──► MongoDB (users, usage history)
                    │
                    ├─ JwtAuthFilter (stateless JWT, BCrypt)
                    ├─ OptimizationService (rules engine)
                    └─ GeminiAdvisorService ──► Google Gemini API
```

## 📡 API

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Public | Create an account and receive a JWT |
| `POST` | `/api/auth/login` | Public | Log in and receive a JWT |
| `POST` | `/api/optimize` | JWT | Analyse a workload and get recommendations |
| `GET` | `/api/optimize/history?page=0&size=5` | JWT | Paginated history of your analyses |
| `POST` | `/api/ai/advice` | JWT | Gemini-generated advice for an analysis |

Interactive docs: **`/swagger-ui.html`** (OpenAPI spec at `/v3/api-docs`).

**Example request:**

```http
POST /api/optimize
Authorization: Bearer <token>
Content-Type: application/json

{
  "cpuUsage": 18,
  "memoryUsage": 30,
  "storageUsage": 80,
  "provider": "AWS",
  "workloadType": "Dev/Test",
  "monthlyCost": 400
}
```

The response includes `recommendation`, `severity`, `estimatedCostSaving` (%), `estimatedMonthlySavingAmount` and `rationale`. Inputs are validated: utilisation values must be 0–100 and the monthly cost can't be negative.

## 🚀 Run locally

### With Docker Compose (recommended)

```bash
docker compose up --build
```

This starts the app on **http://localhost:8080** together with a MongoDB 7 container.

### With Maven

Requires Java 17 and a running MongoDB.

```bash
export SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/cloudoptimizer
export JWT_SECRET=<at-least-32-character-secret>
export GEMINI_API_KEY=<your-gemini-key>   # optional, needed only for AI advice
./mvnw spring-boot:run
```

### Environment variables

| Variable | Required | Description |
|---|---|---|
| `SPRING_DATA_MONGODB_URI` | ✅ | MongoDB connection string |
| `JWT_SECRET` | ✅ | Secret used to sign JWTs |
| `GEMINI_API_KEY` | Optional | Enables the AI advisor |
| `GEMINI_MODEL` | Optional | Gemini model, defaults to `gemini-2.5-flash` |
| `PORT` | Optional | Server port, defaults to `8080` |

## ☁️ Deployment

Deployed on **Render** using the included `render.yaml` blueprint: a Docker web service with an auto-generated `JWT_SECRET`, and with `SPRING_DATA_MONGODB_URI` and `GEMINI_API_KEY` set as secrets.

## 📁 Project structure

```
src/main/java/com/cloud/optimizer/
├── config/        # Security, CORS, forwarded headers, Mongo URI validation
├── controller/    # Auth, Optimization, AI advisor endpoints
├── exception/     # Global exception handler
├── model/         # Request/response DTOs and Mongo documents
├── repository/    # Spring Data Mongo repositories
├── security/      # JWT filter and utilities
└── service/       # Optimisation rules engine, Gemini client
cloud-optimizer-frontend/   # React 19 single-page app
```

---

Built by **[Mohit Verma](https://mohit-verma-portfolio.vercel.app)** · [LinkedIn](https://www.linkedin.com/in/mohit-verma89) · [GitHub](https://github.com/lazy-coder98)

# AI Support Ticket Triage System

Hey! This is a backend project I built to automate how customer support tickets are sorted and handled. Instead of manually reading and routing every single incoming issue, this app uses Spring AI and Google Gemini to quickly analyze tickets, figure out their priority, and send them to the right department.

---

## What it currently does (Features)
- **AI-Driven Triage:** Reads the ticket title and description, analyzes it using Gemini, and automatically tags its priority (`HIGH`, `MEDIUM`, `LOW`) along with token routing to the appropriate department (`TECH_SUPPORT`, `BILLING`, `GENERAL`).
- **Performance Latency Logging:** Uses SLF4J to track and log how many milliseconds the AI takes to respond, helping monitor API response times and performance.
- **Relational Persistence:** Backed by PostgreSQL to reliably store and manage ticket data.
- **Interactive API Testing:** Integrated Swagger UI for testing, exploring, and documenting all REST endpoints directly in the browser.

---

## Tech Stack
- **Core:** Java, Spring Boot, Spring AI
- **AI Engine:** Google Gemini (`gemini-3.6-flash`)
- **Database:** PostgreSQL (`ticket_triage_db`)
- **API Documentation & Testing:** Swagger UI (Springdoc OpenAPI)
- **Logging & Monitoring:** SLF4J

---

## Future Roadmap & What's Next
Here are the upcoming features and architectural expansions planned for this project:
- **Role-Based Access Control (RBAC):** Implementing secure role-based login (e.g., Admin, Support Agent, Customer) to restrict actions and manage tickets based on user permissions.
- **High-Traffic Handling via Apache Kafka:** Integrating event-driven architecture using Kafka to asynchronously handle heavy spikes in incoming support tickets without slowing down the core API.
- **Cloud Deployment:** Hosting the complete backend stack live on a cloud platform (like Render or Railway) so the triage API becomes globally accessible.
- **Automated Email Notifications:** Triggering instant email alerts to respective departments whenever a `HIGH` priority ticket is logged.

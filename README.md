# DayGuard

DayGuard is a secure and privacy-focused communication platform built with Spring Boot and its extensions. It leverages modern web technologies to provide a real-time, encrypted messaging experience.

## Features
- End-to-end encrypted messaging (Coming soon !)
- Limited message storing on database (to ensure private communication) (Coming soon !)
- Real-time communication via WebSockets
- Secure user authentication and session management (Coming soon !)
- Scalable backend using Spring Boot

## Technologies Used
- **Spring Boot** – Core framework for the backend
- **Spring WebSockets** – Enables real-time communication
- **Spring Security** – Handles authentication and authorization (Coming soon !)
- **Spring Data JPA** – Simplifies database interactions
- **JDBC API** – Java Database Connectivity for database operations
- **Spring Data JDBC** – Simplifies database interactions
- **PostgreSQL** – Database support (Coming soon !)
- **Nginx** - Used for enabling HTTPS by using reverse proxy to the Spring-boot server
- **RabbitMQ** - Used for handling queues of the messages
- **Docker** – Containerized deployment

## Installation
Look at [Prerequisites](#prerequisites) <br/>
*(Coming Soon!)*

**Compose containers from `compose.yaml`**

```bash
    docker compose up -d
```

### Prerequisites
- Java 21+
- Maven 3+
- Docker, Docker Compose (For linux users)

### Steps to Run
*(Coming Soon!)*

## Configuration
*(Coming Soon!)*

## Disclaimer
DayGuard is provided "as is" without any warranties. The author assumes no responsibility for any issues, damages, or data loss resulting from the use of this software.

## Future Plans (Sorted by priority)
- Implement online website for front-end (Current frontend styling is written by AI, but this is only temporary)
- Implement account functionality
- Support for group messaging
- Support private messaging
- Advanced encryption options
- Implement mobile and web clients
- Push notifications

---
For contributions, bug reports, or feature requests, feel free to open an issue on GitHub.
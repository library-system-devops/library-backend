# Library System Backend

Spring Boot backend implementation for the Library Management System. This repository provides the REST API and business logic for library operations.

## Quick Overview

- Spring Boot 3.x
- MySQL database
- JWT authentication
- Role-based security
- REST API endpoints

## Local Development

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Run tests
mvn test
```

## Database Setup

Initial database script is provided in `initial_setup.sql`.

## API Endpoints

Main endpoint groups:
- `/api/auth/*` - Authentication
- `/api/books/*` - Book management
- `/api/users/*` - User management
- `/api/loans/*` - Loan management
- `/api/reservations/*` - Reservation management

## Deployment & Documentation

For complete documentation including:
- System architecture
- Deployment procedures
- Environment setup
- Security considerations
- Monitoring and maintenance

Please refer to the main [Library System DevOps Repository](https://github.com/library-system-devops/library-devops).
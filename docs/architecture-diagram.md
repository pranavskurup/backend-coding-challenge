# Movie Rating System - Simplified Architecture

## Overview
Reactive Spring Boot application with Clean Architecture, providing movie rating APIs with JWT authentication.

## System Architecture

```mermaid
graph TB
    subgraph "Client"
        UI[Web/Mobile Apps]
        API[API Clients]
    end

    subgraph "Web Layer"
        Router[Routers]
        Handler[Handlers]
        Filter[JWT Filter]
    end

    subgraph "Service Layer"
        Auth[Auth Service]
        User[User Service]
        Movie[Movie Service]
        Rating[Rating Service]
    end

    subgraph "Domain"
        UserD[User]
        MovieD[Movie]
        RatingD[Rating]
        TokenD[JWT Token]
    end

    subgraph "Data"
        Repo[R2DBC Repos]
        DB[(PostgreSQL)]
    end

    UI --> Router
    API --> Router
    Router --> Handler
    Filter --> Handler
    Handler --> Auth
    Handler --> User
    Handler --> Movie
    Handler --> Rating
    
    Auth --> UserD
    User --> UserD
    Movie --> MovieD
    Rating --> RatingD
    Auth --> TokenD
    
    Auth --> Repo
    User --> Repo
    Movie --> Repo
    Rating --> Repo
    Repo --> DB

    classDef web fill:#e3f2fd
    classDef service fill:#f3e5f5
    classDef domain fill:#e8f5e8
    classDef data fill:#fff3e0

    class Router,Handler,Filter web
    class Auth,User,Movie,Rating service
    class UserD,MovieD,RatingD,TokenD domain
    class Repo,DB data
```

## Data Model

```mermaid
erDiagram
    users {
        uuid id PK
        string username
        string email
        string password_hash
        boolean is_active
        timestamp created_at
    }
    
    movies {
        uuid id PK
        string title
        text plot
        int year_of_release
        uuid created_by FK
        timestamp created_at
    }
    
    movie_ratings {
        uuid id PK
        uuid movie_id FK
        uuid user_id FK
        int rating
        text review
        timestamp created_at
    }
    
    jwt_tokens {
        uuid id PK
        uuid user_id FK
        string token_hash
        timestamp expires_at
    }

    users ||--o{ movies : creates
    users ||--o{ movie_ratings : rates
    movies ||--o{ movie_ratings : rated
    users ||--o{ jwt_tokens : has
```

## API Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant D as Database

    Note over C,D: Login Flow
    C->>R: POST /auth/login
    R->>H: authenticate
    H->>S: validate credentials
    S->>D: find user
    D-->>S: user data
    S-->>H: JWT tokens
    H-->>C: 200 + tokens

    Note over C,D: Rate Movie Flow
    C->>R: POST /movies/{id}/ratings
    R->>H: rate movie (with JWT)
    H->>S: create rating
    S->>D: save rating
    D-->>S: success
    S-->>H: rating data
    H-->>C: 200 + rating
```

## Tech Stack
- **Framework**: Spring Boot 3.5 + WebFlux
- **Database**: PostgreSQL + R2DBC
- **Security**: JWT + BCrypt
- **Docs**: OpenAPI/Swagger
- **Build**: Maven + Java 21

## Key Features
- ✅ Reactive programming
- ✅ Clean architecture
- ✅ JWT authentication
- ✅ User management
- ✅ Movie CRUD
- ✅ Rating system
- ✅ API documentation

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

## API Workflows

### Authentication Flows

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant DB as Database

    Note over C,DB: User Registration
    C->>R: POST /api/v1/auth/register
    R->>H: register(request)
    H->>S: registerUser(userData)
    S->>DB: save user
    DB-->>S: user created
    S-->>H: user response
    H-->>C: 201 + user data

    Note over C,DB: User Login
    C->>R: POST /api/v1/auth/login
    R->>H: login(credentials)
    H->>S: authenticate(username, password)
    S->>DB: find user by username/email
    DB-->>S: user data
    S->>S: validate password
    S->>S: generate JWT tokens
    S->>DB: save refresh token
    DB-->>S: token saved
    S-->>H: authentication response
    H-->>C: 200 + access/refresh tokens

    Note over C,DB: Token Refresh
    C->>R: POST /api/v1/auth/refresh
    R->>H: refreshToken(refreshToken)
    H->>S: refreshAccessToken(token)
    S->>DB: validate refresh token
    DB-->>S: token valid
    S->>S: generate new access token
    S-->>H: new tokens
    H-->>C: 200 + new access token

    Note over C,DB: User Logout
    C->>R: POST /api/v1/auth/logout (with JWT)
    R->>H: logout(request)
    H->>S: invalidateTokens(userId)
    S->>DB: revoke all user tokens
    DB-->>S: tokens revoked
    S-->>H: success response
    H-->>C: 200 + logout confirmation
```

### User Management Flows

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant DB as Database

    Note over C,DB: Check Username Availability
    C->>R: GET /api/v1/users/check/username?username=john
    R->>H: checkUsernameAvailability(username)
    H->>S: isUsernameAvailable(username)
    S->>DB: find user by username
    DB-->>S: user exists/not exists
    S-->>H: availability status
    H-->>C: 200 + {available: true/false}

    Note over C,DB: Get User Profile (Own Profile)
    C->>R: GET /api/v1/users/{userId}/profile (with JWT, own ID)
    R->>H: getUserProfile(userId)
    H->>S: getUserProfile(userId, requesterId)
    S->>S: check if requesting own profile
    S->>DB: find user by id
    DB-->>S: user data
    S-->>H: full user profile (including email, etc.)
    H-->>C: 200 + complete profile data

    Note over C,DB: Get User Profile (Other User)
    C->>R: GET /api/v1/users/{userId}/profile (with JWT, other user ID)
    R->>H: getUserProfile(userId)
    H->>S: getUserProfile(userId, requesterId)
    S->>S: check if requesting other user's profile
    S->>DB: find user by id
    DB-->>S: user data
    S-->>H: limited user profile (firstName, lastName, username only)
    H-->>C: 200 + limited profile data

    Note over C,DB: Update User Profile
    C->>R: PUT /api/v1/users/{userId}/profile (with JWT)
    R->>H: updateUserProfile(userId, data)
    H->>S: updateProfile(userId, updates)
    S->>S: validate permissions
    S->>DB: update user data
    DB-->>S: updated user
    S-->>H: updated profile
    H-->>C: 200 + updated profile

    Note over C,DB: Change Password
    C->>R: POST /api/v1/users/{userId}/change-password (with JWT)
    R->>H: changePassword(userId, passwordData)
    H->>S: changePassword(userId, oldPassword, newPassword)
    S->>DB: find user
    DB-->>S: user data
    S->>S: validate old password
    S->>S: hash new password
    S->>DB: update password
    DB-->>S: password updated
    S-->>H: success response
    H-->>C: 200 + confirmation
```

### Movie Management Flows

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant DB as Database

    Note over C,DB: Create Movie
    C->>R: POST /api/v1/movies (with JWT)
    R->>H: createMovie(movieData)
    H->>S: createMovie(movie, userId)
    S->>S: validate movie data
    S->>DB: save movie
    DB-->>S: movie created
    S-->>H: movie response
    H-->>C: 201 + movie data

    Note over C,DB: Get All Movies
    C->>R: GET /api/v1/movies
    R->>H: getAllMovies(request)
    H->>S: getAllActiveMovies(page, size)
    S->>DB: find active movies with pagination
    DB-->>S: movies list
    S-->>H: paginated movies
    H-->>C: 200 + movies + pagination info

    Note over C,DB: Search Movies by Title
    C->>R: GET /api/v1/movies/search?title=avengers
    R->>H: searchMoviesByTitle(title)
    H->>S: searchByTitle(title)
    S->>DB: find movies where title contains
    DB-->>S: matching movies
    S-->>H: search results
    H-->>C: 200 + matching movies

    Note over C,DB: Get Movie by ID
    C->>R: GET /api/v1/movies/{id}
    R->>H: getMovieById(movieId)
    H->>S: findMovieById(movieId)
    S->>DB: find movie by id
    DB-->>S: movie data
    S-->>H: movie details
    H-->>C: 200 + movie details

    Note over C,DB: Update Movie
    C->>R: PUT /api/v1/movies/{id} (with JWT)
    R->>H: updateMovie(movieId, updates)
    H->>S: updateMovie(movieId, updates, userId)
    S->>S: validate ownership
    S->>DB: update movie
    DB-->>S: updated movie
    S-->>H: updated movie
    H-->>C: 200 + updated movie

    Note over C,DB: Delete Movie
    C->>R: DELETE /api/v1/movies/{id} (with JWT)
    R->>H: deleteMovie(movieId)
    H->>S: deleteMovie(movieId, userId)
    S->>S: validate ownership
    S->>DB: soft delete movie
    DB-->>S: movie deactivated
    S-->>H: success response
    H-->>C: 204 No Content
```

### Movie Rating Flows

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant DB as Database

    Note over C,DB: Create Movie Rating
    C->>R: POST /api/v1/ratings (with JWT)
    R->>H: createRating(ratingData)
    H->>S: createRating(rating, userId)
    S->>DB: check existing rating
    DB-->>S: no existing rating
    S->>S: validate rating (1-10)
    S->>DB: save rating
    DB-->>S: rating created
    S-->>H: rating response
    H-->>C: 201 + rating data

    Note over C,DB: Get Movie Ratings
    C->>R: GET /api/v1/movies/{movieId}/ratings
    R->>H: getRatingsByMovie(movieId)
    H->>S: getMovieRatings(movieId, page, size)
    S->>DB: find ratings for movie
    DB-->>S: ratings list
    S-->>H: paginated ratings
    H-->>C: 200 + ratings + pagination

    Note over C,DB: Get Movie Rating Stats
    C->>R: GET /api/v1/movies/{movieId}/ratings/stats
    R->>H: getMovieRatingStats(movieId)
    H->>S: calculateRatingStats(movieId)
    S->>DB: aggregate rating statistics
    DB-->>S: stats (avg, count, distribution)
    S-->>H: rating statistics
    H-->>C: 200 + {average, count, distribution}

    Note over C,DB: Get My Ratings
    C->>R: GET /api/v1/ratings/my (with JWT)
    R->>H: getMyRatings(request)
    H->>S: getUserRatings(userId, page, size)
    S->>DB: find user's ratings
    DB-->>S: user ratings
    S-->>H: paginated ratings
    H-->>C: 200 + user's ratings

    Note over C,DB: Update Rating
    C->>R: PUT /api/v1/ratings/{id} (with JWT)
    R->>H: updateRating(ratingId, updates)
    H->>S: updateRating(ratingId, updates, userId)
    S->>S: validate ownership
    S->>DB: update rating
    DB-->>S: updated rating
    S-->>H: updated rating
    H-->>C: 200 + updated rating

    Note over C,DB: Delete Rating
    C->>R: DELETE /api/v1/ratings/{id} (with JWT)
    R->>H: deleteRating(ratingId)
    H->>S: deleteRating(ratingId, userId)
    S->>S: validate ownership
    S->>DB: soft delete rating
    DB-->>S: rating deactivated
    S-->>H: success response
    H-->>C: 204 No Content
```

### User Management Flows (Public Discovery)

> **TODO**: Admin-specific workflows (user role management, admin-only operations) are not fully implemented due to time constraints. The current implementation treats all authenticated users equally for user discovery features. In a production system, admin roles would control access to user management operations.

```mermaid
sequenceDiagram
    participant C as Client
    participant R as Router
    participant H as Handler
    participant S as Service
    participant DB as Database

    Note over C,DB: Get All Users (Mixed Profile Data)
    C->>R: GET /api/v1/users (with JWT)
    R->>H: getAllActiveUsers(request)
    H->>S: getAllActiveUsers()
    S->>DB: find active users
    DB-->>S: users list
    S-->>H: users data
    H->>H: check each user - full profile for self, limited for others
    H-->>C: 200 + mixed profile data (full for self, limited for others)

    Note over C,DB: Search Users by Username (Mixed Profile Data)
    C->>R: GET /api/v1/users/search?username=john (with JWT)
    R->>H: searchUsersByUsername(username)
    H->>S: searchByUsername(username)
    S->>DB: find users where username contains pattern
    DB-->>S: matching users
    S-->>H: users data
    H->>H: check each user - full profile for self, limited for others
    H-->>C: 200 + mixed profile data (full for self, limited for others)

    Note over C,DB: Reactivate User (Self-Service Only)
    C->>R: POST /api/v1/users/{userId}/reactivate (with JWT)
    R->>H: reactivateUser(userId)
    H->>S: reactivateUser(userId, requesterId)
    S->>S: validate user can reactivate own account
    S->>DB: reactivate user
    DB-->>S: user reactivated
    S-->>H: success response
    H-->>C: 200 + confirmation
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

## Implementation Notes

### Completed Features
- ✅ User authentication with JWT (access/refresh tokens)
- ✅ User registration and profile management
- ✅ Movie CRUD operations with ownership validation
- ✅ Movie rating system with duplicate prevention
- ✅ Privacy-aware user profile access (full for self, limited for others)
- ✅ Reactive programming with Spring WebFlux
- ✅ Clean architecture with domain-driven design
- ✅ Comprehensive API documentation with OpenAPI/Swagger
- ✅ Database migrations with Flyway
- ✅ Unit and integration tests

### TODO / Not Implemented (Time Constraints)
- ❌ **Admin Role System**: No role-based access control implemented
  - All authenticated users have same permissions
  - Admin-specific endpoints exist but don't enforce admin roles
  - Future: Implement User roles (ADMIN, USER) with proper authorization
- ❌ **Caching Layer**: No Redis/cache implementation for performance
  - Movie ratings, user profiles could benefit from caching
  - Future: Add Redis for frequently accessed data
- ❌ **Advanced Search**: Basic search only
  - Movie search by genre, rating range, actor not implemented
  - User search only by username pattern
  - Future: Implement Elasticsearch or advanced SQL queries
- ❌ **Pagination**: Limited pagination support
  - Some endpoints return all results without pagination
  - Future: Implement consistent pagination across all list endpoints
- ❌ **Rate Limiting**: No API rate limiting implemented
  - Future: Add rate limiting to prevent abuse
- ❌ **Email Notifications**: No email service integration
  - Future: Send welcome emails, password reset notifications
- ❌ **Audit Logging**: Basic logging only, no audit trail
  - Future: Implement comprehensive audit logging for all operations

-- Create movies table
CREATE TABLE movies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre VARCHAR(100),
    release_year INTEGER,
    director VARCHAR(255),
    duration_minutes INTEGER,
    owner_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    rating_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_genre ON movies(genre);
CREATE INDEX idx_movies_release_year ON movies(release_year);
CREATE INDEX idx_movies_owner_id ON movies(owner_id);
CREATE INDEX idx_movies_is_active ON movies(is_active);
CREATE INDEX idx_movies_rating_enabled ON movies(rating_enabled);
CREATE INDEX idx_movies_created_at ON movies(created_at);

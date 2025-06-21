-- Create movie ratings table
CREATE TABLE movie_ratings
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    movie_id         UUID         NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating           INTEGER      NOT NULL CHECK (rating >= 1 AND rating <= 10),
    review           TEXT,
    is_active        BOOLEAN      NOT NULL DEFAULT true,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure one rating per user per movie
    UNIQUE(movie_id, user_id)
);

-- Create indexes for performance
CREATE INDEX idx_movie_ratings_movie_id ON movie_ratings (movie_id);
CREATE INDEX idx_movie_ratings_user_id ON movie_ratings (user_id);
CREATE INDEX idx_movie_ratings_rating ON movie_ratings (rating);
CREATE INDEX idx_movie_ratings_is_active ON movie_ratings (is_active);
CREATE INDEX idx_movie_ratings_created_at ON movie_ratings (created_at);

-- Composite indexes for common queries
CREATE INDEX idx_movie_ratings_movie_active ON movie_ratings (movie_id, is_active);
CREATE INDEX idx_movie_ratings_user_active ON movie_ratings (user_id, is_active);
CREATE INDEX idx_movie_ratings_active_rating ON movie_ratings (is_active, rating);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_movie_ratings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update the updated_at field
CREATE TRIGGER trigger_update_movie_ratings_updated_at
    BEFORE UPDATE ON movie_ratings
    FOR EACH ROW
    EXECUTE FUNCTION update_movie_ratings_updated_at();

-- Create a view for movie statistics
CREATE VIEW movie_rating_stats AS
SELECT 
    m.id as movie_id,
    m.title,
    m.year_of_release,
    COUNT(mr.rating) as total_ratings,
    ROUND(AVG(mr.rating::decimal), 2) as average_rating,
    MIN(mr.rating) as min_rating,
    MAX(mr.rating) as max_rating,
    COUNT(CASE WHEN mr.rating >= 8 THEN 1 END) as excellent_ratings,
    COUNT(CASE WHEN mr.rating >= 6 AND mr.rating < 8 THEN 1 END) as good_ratings,
    COUNT(CASE WHEN mr.rating < 6 THEN 1 END) as poor_ratings
FROM movies m
LEFT JOIN movie_ratings mr ON m.id = mr.movie_id AND mr.is_active = true
WHERE m.is_active = true
GROUP BY m.id, m.title, m.year_of_release;

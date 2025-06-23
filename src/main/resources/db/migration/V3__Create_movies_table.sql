-- Create movies table
CREATE TABLE movies
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title            VARCHAR(255) NOT NULL,
    plot             TEXT,
    year_of_release  INTEGER      NOT NULL CHECK (year_of_release > 1800 AND year_of_release <= EXTRACT(YEAR FROM CURRENT_DATE) + 5),
    is_active        BOOLEAN      NOT NULL DEFAULT true,
    created_by       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_at   TIMESTAMP,
    deactivated_by   UUID         REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_movies_title ON movies (title);
CREATE INDEX idx_movies_year_of_release ON movies (year_of_release);
CREATE INDEX idx_movies_is_active ON movies (is_active);
CREATE INDEX idx_movies_created_by ON movies (created_by);
CREATE INDEX idx_movies_created_at ON movies (created_at);

-- Composite indexes for common queries
CREATE INDEX idx_movies_active_title ON movies (is_active, title);
CREATE INDEX idx_movies_active_year ON movies (is_active, year_of_release);
CREATE INDEX idx_movies_user_active ON movies (created_by, is_active);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_movies_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update the updated_at field
CREATE TRIGGER trigger_update_movies_updated_at
    BEFORE UPDATE ON movies
    FOR EACH ROW
    EXECUTE FUNCTION update_movies_updated_at();

-- Add constraint to ensure deactivated_at is set when is_active is false
ALTER TABLE movies ADD CONSTRAINT check_deactivated_consistency 
    CHECK ((is_active = true AND deactivated_at IS NULL) OR 
           (is_active = false AND deactivated_at IS NOT NULL));

-- Event types table
CREATE TABLE event_type (
    event_type_id uuid PRIMARY KEY,
    label varchar(255) NOT NULL UNIQUE
);

-- Event categories table
CREATE TABLE event_category (
    category_id uuid PRIMARY KEY,
    label varchar(255) NOT NULL UNIQUE
);

-- Events table
CREATE TABLE event (
    event_id uuid PRIMARY KEY,
    name varchar(255) NOT NULL,
    description text,
    start_date date NOT NULL,
    end_date date NOT NULL,
    location varchar(255),
    full_address varchar(512),
    event_type_id uuid REFERENCES event_type(event_type_id),
    category_id uuid REFERENCES event_category(category_id),
    image_url varchar(512),
    status varchar(20) NOT NULL CHECK (status IN ('active','canceled','full')),
    creator_id uuid NOT NULL,
    creation_date date NOT NULL,
    CONSTRAINT valid_dates CHECK (end_date >= start_date)
);

-- Favorites table
CREATE TABLE favorite (
    favorite_id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    event_id uuid REFERENCES event(event_id) ON DELETE CASCADE,
    added_date date NOT NULL,
    UNIQUE(user_id, event_id)
);

-- Indexes for performance optimization
CREATE INDEX idx_event_status ON event(status);
CREATE INDEX idx_event_dates ON event(start_date, end_date);
CREATE INDEX idx_favorite_user ON favorite(user_id);
CREATE INDEX idx_event_creator ON event(creator_id);


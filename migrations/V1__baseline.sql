
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR (14) NOT NULL UNIQUE,
    name TEXT NOT NULL,
    gender TEXT NOT NULL,
    birth_date DATE NOT NULL,
    hashed_password TEXT NOT NULL,
    verified_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE user_otps (
    user_id UUID NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    value SMALLINT NOT NULL DEFAULT floor(1000 + random() * 8999),
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
               DEFAULT (NOW() AT TIME ZONE 'utc') + '1 hour'
);

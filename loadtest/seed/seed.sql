CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

SELECT set_config('seed.users', :'users', false);

DO $$
DECLARE
    users_count int := current_setting('seed.users')::int;
BEGIN
    IF EXISTS (SELECT 1 FROM "user" WHERE name LIKE 'Load User %') THEN
        RAISE NOTICE 'Load users already exist, seed skipped';
        RETURN;
    END IF;

    INSERT INTO "user" (name, date_of_birth, password)
    SELECT 'Load User ' || g, DATE '1960-01-01' + (g % 15000), 'password123'
    FROM generate_series(1, users_count) AS g
    ORDER BY g;

    INSERT INTO account (user_id, balance, initial_balance)
    SELECT id, 100000.00, 100000.00
    FROM "user"
    WHERE name LIKE 'Load User %';

    INSERT INTO email_data (user_id, email)
    SELECT id, 'load' || substring(name FROM 11) || '@example.com'
    FROM "user"
    WHERE name LIKE 'Load User %';

    INSERT INTO phone_data (user_id, phone)
    SELECT id, '7900' || lpad(substring(name FROM 11), 7, '0')
    FROM "user"
    WHERE name LIKE 'Load User %';

    RAISE NOTICE 'Seeded % load users', users_count;
END $$;

ANALYZE;

SELECT min(id) AS first_load_user_id, max(id) AS last_load_user_id, count(*) AS load_users
FROM "user"
WHERE name LIKE 'Load User %';

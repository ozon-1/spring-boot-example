CREATE TYPE Role AS ENUM ('USER','ADMIN', 'MANAGER');

ALTER TABLE customer
ADD Role role NOT NULL
DEFAULT('USER');

CREATE FUNCTION role_cast(varchar) RETURNS Role AS $$
    SELECT CASE $1
        WHEN 'USER' THEN 'USER'::Role
        WHEN 'ADMIN' THEN 'ADMIN'::Role
        WHEN 'MANAGER' THEN 'MANAGER'::Role
    END;
$$ LANGUAGE SQL;

CREATE CAST (varchar AS Role) WITH FUNCTION role_cast(varchar) AS ASSIGNMENT;
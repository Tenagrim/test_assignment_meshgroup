-- init-debezium.sql
CREATE USER debezium WITH LOGIN REPLICATION PASSWORD 'debezium_password';
GRANT USAGE ON SCHEMA public TO debezium;


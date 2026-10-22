-- init-debezium.sql
CREATE USER debezium WITH LOGIN REPLICATION PASSWORD 'debezium_password';
GRANT USAGE ON SCHEMA public TO debezium;
ALTER DEFAULT PRIVILEGES FOR ROLE test_assg IN SCHEMA public GRANT SELECT ON TABLES TO debezium;

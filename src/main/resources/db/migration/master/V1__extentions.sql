-- ====================================================================================
-- MIGRATION SCRIPT: V1__extentions.sql
-- ====================================================================================

-- ------------------------------------------------------------------------------------
-- EXTENSION 1: pgcrypto
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Enables PostgreSQL cryptographic functions, secure hashing, and random UUID generation
--   functions like `gen_random_uuid()`.
--
-- [WHY IMPLEMENTED]:
--   Primary keys in tables (institutions, schools, users, roles) use UUIDs instead of auto-incrementing integers.
--   Using `gen_random_uuid()` directly at the database level eliminates ID collision risks across multi-tenant
--   schemas and prevents sequence enumeration attacks.
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   Table creation statements using `DEFAULT gen_random_uuid()` will fail with a database error:
--   "function gen_random_uuid() does not exist", preventing schema initialization and record insertion.
-- ------------------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ------------------------------------------------------------------------------------
-- EXTENSION 2: citext (Case-Insensitive Text)
-- ------------------------------------------------------------------------------------
-- [FUNCTIONALITY]:
--   Provides the `CITEXT` data type, which compares string values case-insensitively while preserving original case.
--
-- [WHY IMPLEMENTED]:
--   Essential for business identifiers like emails (`john@school.com` vs `John@School.com`), usernames, school codes,
--   and institution names. It enforces unique constraints regardless of casing without requiring complex lower-case indexes.
--
-- [WHAT HAPPENS IF NOT IMPLEMENTED]:
--   Columns declared as `CITEXT` will throw column type definition errors ("type citext does not exist").
--   Without case-insensitive uniqueness, duplicate accounts could be created using different capitalization,
--   causing severe authentication bugs and security vulnerabilities.
-- ------------------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS citext;

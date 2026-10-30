-- Botica: login SQL que usa la aplicación Java (JDBC)
-- Requisitos: SQL Server en modo de autenticación mixta y TCP/IP habilitado (puerto 1433).
-- Ejecutar DESPUÉS de Botica_Base_de_Datos.sql. La contraseña debe coincidir con config/db.properties.
USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'botica_app')
    CREATE LOGIN botica_app WITH PASSWORD = 'Botica2026!', CHECK_POLICY = OFF, DEFAULT_DATABASE = BoticaDB;
GO

USE BoticaDB;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'botica_app')
    CREATE USER botica_app FOR LOGIN botica_app;
GO
ALTER ROLE db_owner ADD MEMBER botica_app;
GO
-- Necesario para generar copias de seguridad (HU-029)
USE master;
GO
ALTER SERVER ROLE dbcreator ADD MEMBER botica_app;
GO

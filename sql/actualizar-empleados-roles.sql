SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Roles', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.Roles (
            IdRol INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_Roles PRIMARY KEY,
            Rol VARCHAR(50) NOT NULL
                CONSTRAINT UQ_Roles_Rol UNIQUE
        );
    END;

    IF COL_LENGTH(N'dbo.Empleados', N'ContrasenaHash') IS NULL
        ALTER TABLE dbo.Empleados ADD ContrasenaHash VARCHAR(255) NULL;

    IF COL_LENGTH(N'dbo.Empleados', N'Estado') IS NULL
        ALTER TABLE dbo.Empleados ADD Estado VARCHAR(20) NOT NULL
            CONSTRAINT DF_Empleados_Estado DEFAULT 'Activo' WITH VALUES;

    IF COL_LENGTH(N'dbo.Empleados', N'IdRol') IS NULL
        ALTER TABLE dbo.Empleados ADD IdRol INT NULL;

    IF COL_LENGTH(N'dbo.Empleados', N'Rol') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            INSERT INTO dbo.Roles (Rol)
            SELECT DISTINCT LTRIM(RTRIM(E.Rol))
            FROM dbo.Empleados AS E
            WHERE NULLIF(LTRIM(RTRIM(E.Rol)), '''') IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.Roles AS R
                  WHERE R.Rol = LTRIM(RTRIM(E.Rol))
              );';
        EXEC sys.sp_executesql N'
            UPDATE E
            SET E.IdRol = R.IdRol
            FROM dbo.Empleados AS E
            INNER JOIN dbo.Roles AS R
                ON R.Rol = COALESCE(NULLIF(LTRIM(RTRIM(E.Rol)), ''''), ''Sin asignar'')
            WHERE E.IdRol IS NULL;';
    END;

    INSERT INTO dbo.Roles (Rol)
    SELECT V.Rol
    FROM (VALUES ('Producción'), ('Inventario'), ('Ventas'), ('Jefe'), ('Sin asignar')) AS V(Rol)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Roles AS R WHERE R.Rol = V.Rol);

    DECLARE @empleadosSql NVARCHAR(MAX) = N'
        UPDATE dbo.Empleados
        SET IdRol = (SELECT IdRol FROM dbo.Roles WHERE Rol = ''Sin asignar'')
        WHERE IdRol IS NULL;

        IF EXISTS (SELECT 1 FROM dbo.Empleados WHERE IdRol IS NULL)
            THROW 51000, ''No se pudo asignar un rol a todos los empleados existentes.'', 1;

        ALTER TABLE dbo.Empleados ALTER COLUMN IdRol INT NOT NULL;

        IF NOT EXISTS (
            SELECT 1
            FROM sys.foreign_keys
            WHERE name = N''FK_Empleados_Roles''
              AND parent_object_id = OBJECT_ID(N''dbo.Empleados'')
        )
            ALTER TABLE dbo.Empleados
                ADD CONSTRAINT FK_Empleados_Roles
                FOREIGN KEY (IdRol) REFERENCES dbo.Roles (IdRol);

        IF COL_LENGTH(N''dbo.Empleados'', N''Rol'') IS NOT NULL
            ALTER TABLE dbo.Empleados DROP COLUMN Rol;

        IF NOT EXISTS (
            SELECT 1
            FROM sys.check_constraints
            WHERE name = N''CK_Empleados_Estado''
              AND parent_object_id = OBJECT_ID(N''dbo.Empleados'')
        )
            ALTER TABLE dbo.Empleados
                ADD CONSTRAINT CK_Empleados_Estado
                CHECK (Estado IN (''Activo'', ''Inactivo''));
    ';
    EXEC sys.sp_executesql @empleadosSql;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

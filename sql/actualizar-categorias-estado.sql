USE Inventario;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.Categorias', N'Estado') IS NULL
    BEGIN
        ALTER TABLE dbo.Categorias
            ADD Estado VARCHAR(20) NOT NULL
                CONSTRAINT DF_Categorias_Estado DEFAULT 'Activo' WITH VALUES;
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = N'CK_Categorias_Estado'
          AND parent_object_id = OBJECT_ID(N'dbo.Categorias')
    )
    BEGIN
        ALTER TABLE dbo.Categorias
            ADD CONSTRAINT CK_Categorias_Estado
            CHECK (Estado IN ('Activo', 'Inactivo'));
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

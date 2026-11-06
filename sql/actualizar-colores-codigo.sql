USE Inventario;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.Colores', N'CodigoHex') IS NULL
    BEGIN
        ALTER TABLE dbo.Colores
            ADD CodigoHex VARCHAR(7) NOT NULL
                CONSTRAINT DF_Colores_CodigoHex DEFAULT '#14B8A6' WITH VALUES;
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = N'CK_Colores_CodigoHex'
          AND parent_object_id = OBJECT_ID(N'dbo.Colores')
    )
    BEGIN
        ALTER TABLE dbo.Colores
            ADD CONSTRAINT CK_Colores_CodigoHex
            CHECK (CodigoHex LIKE '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]');
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

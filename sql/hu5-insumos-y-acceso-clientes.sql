USE Inventario;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.CredencialesUsuarios', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.CredencialesUsuarios (
            UsuarioId INT NOT NULL
                CONSTRAINT PK_CredencialesUsuarios PRIMARY KEY,
            ContrasenaHash VARCHAR(255) NOT NULL,
            FechaCreacion DATETIME2 NOT NULL
                CONSTRAINT DF_CredencialesUsuarios_FechaCreacion DEFAULT GETUTCDATE(),
            CONSTRAINT FK_CredencialesUsuarios_Usuarios
                FOREIGN KEY (UsuarioId) REFERENCES dbo.Usuarios (UsuarioId)
        );
    END;

    IF OBJECT_ID(N'dbo.InsumosMenores', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.InsumosMenores (
            InsumoId INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_InsumosMenores PRIMARY KEY,
            Nombre VARCHAR(100) NOT NULL,
            Referencia VARCHAR(50) NOT NULL,
            UnidadMedida VARCHAR(30) NOT NULL,
            CostoUnitario DECIMAL(10,2) NOT NULL,
            CantidadDisponible DECIMAL(10,2) NOT NULL,
            CONSTRAINT UQ_InsumosMenores_Referencia UNIQUE (Referencia),
            CONSTRAINT CK_InsumosMenores_CostoUnitario CHECK (CostoUnitario >= 0),
            CONSTRAINT CK_InsumosMenores_CantidadDisponible CHECK (CantidadDisponible >= 0)
        );
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

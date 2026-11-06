USE Inventario;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.CatalogoTallas', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.CatalogoTallas (
            TallaId INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_CatalogoTallas PRIMARY KEY,
            Codigo VARCHAR(20) NOT NULL,
            NombreDescriptivo VARCHAR(100) NOT NULL,
            Sistema VARCHAR(30) NOT NULL,
            OrdenVisual INT NOT NULL,
            Estado VARCHAR(20) NOT NULL
                CONSTRAINT DF_CatalogoTallas_Estado DEFAULT 'Activo',
            CONSTRAINT UQ_CatalogoTallas_Codigo_Sistema UNIQUE (Codigo, Sistema),
            CONSTRAINT CK_CatalogoTallas_Sistema
                CHECK (Sistema IN ('Adulto', 'Infantil', 'Sin definir')),
            CONSTRAINT CK_CatalogoTallas_OrdenVisual CHECK (OrdenVisual >= 0),
            CONSTRAINT CK_CatalogoTallas_Estado CHECK (Estado IN ('Activo', 'Inactivo'))
        );
    END;

    IF OBJECT_ID(N'dbo.ProductoTallas', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.ProductoTallas (
            ProductoTallaId INT IDENTITY(1,1) NOT NULL
                CONSTRAINT PK_ProductoTallas PRIMARY KEY,
            ProductoId INT NOT NULL,
            TallaId INT NOT NULL,
            CONSTRAINT UQ_ProductoTallas_Producto_Talla UNIQUE (ProductoId, TallaId),
            CONSTRAINT FK_ProductoTallas_Productos
                FOREIGN KEY (ProductoId) REFERENCES dbo.Productos (ProductoId),
            CONSTRAINT FK_ProductoTallas_CatalogoTallas
                FOREIGN KEY (TallaId) REFERENCES dbo.CatalogoTallas (TallaId)
        );
    END;

    IF OBJECT_ID(N'dbo.Tallas', N'U') IS NOT NULL
       AND COL_LENGTH(N'dbo.Tallas', N'ProductoId') IS NOT NULL
       AND COL_LENGTH(N'dbo.Tallas', N'Talla') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            ;WITH TallasExistentes AS (
                SELECT
                    MIN(TallaId) AS PrimerId,
                    MIN(LTRIM(RTRIM(Talla))) AS NombreDescriptivo,
                    UPPER(LTRIM(RTRIM(Talla))) AS NombreNormalizado
                FROM dbo.Tallas
                WHERE NULLIF(LTRIM(RTRIM(Talla)), '''') IS NOT NULL
                GROUP BY UPPER(LTRIM(RTRIM(Talla)))
            ),
            TallasNumeradas AS (
                SELECT
                    PrimerId,
                    NombreDescriptivo,
                    NombreNormalizado,
                    ROW_NUMBER() OVER (ORDER BY PrimerId) AS Numero
                FROM TallasExistentes
            )
            INSERT INTO dbo.CatalogoTallas
                (Codigo, NombreDescriptivo, Sistema, OrdenVisual, Estado)
            SELECT
                ''LEG'' + RIGHT(''00000'' + CONVERT(VARCHAR(5), Numero), 5),
                LEFT(NombreDescriptivo, 100),
                ''Sin definir'',
                CONVERT(INT, Numero),
                ''Activo''
            FROM TallasNumeradas AS existente
            WHERE NOT EXISTS (
                SELECT 1
                FROM dbo.CatalogoTallas AS catalogo
                WHERE catalogo.Sistema = ''Sin definir''
                  AND UPPER(LTRIM(RTRIM(catalogo.NombreDescriptivo))) = existente.NombreNormalizado
            );

            INSERT INTO dbo.ProductoTallas (ProductoId, TallaId)
            SELECT DISTINCT existente.ProductoId, catalogo.TallaId
            FROM dbo.Tallas AS existente
            INNER JOIN dbo.CatalogoTallas AS catalogo
                ON catalogo.Sistema = ''Sin definir''
               AND UPPER(LTRIM(RTRIM(catalogo.NombreDescriptivo))) =
                   UPPER(LTRIM(RTRIM(existente.Talla)))
            WHERE NULLIF(LTRIM(RTRIM(existente.Talla)), '''') IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1
                  FROM dbo.ProductoTallas AS asignacion
                  WHERE asignacion.ProductoId = existente.ProductoId
                    AND asignacion.TallaId = catalogo.TallaId
              );';
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

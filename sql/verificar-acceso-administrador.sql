USE [inventario];
GO

SELECT
    E.EmpleadoId,
    E.Estado,
    E.IdRol,
    R.Rol,
    CASE
        WHEN NULLIF(LTRIM(RTRIM(E.ContrasenaHash)), '') IS NULL
            THEN 'Falta contraseña'
        WHEN E.ContrasenaHash LIKE 'pbkdf2-sha256$%'
            THEN 'Hash PBKDF2 guardado'
        WHEN CHARINDEX('$', E.ContrasenaHash) > 0
            THEN 'Formato de hash no reconocido'
        ELSE 'Contraseña antigua en texto plano; se migrará al iniciar sesión'
    END AS EstadoContrasena
FROM dbo.Empleados AS E
LEFT JOIN dbo.Roles AS R
    ON R.IdRol = E.IdRol
WHERE E.EmpleadoId = 43987601;

# Inventario de escritorio

Interfaz de escritorio hecha con Electron y React. La aplicacion envia el formulario de productos al endpoint `POST /productos` del backend Spring Boot, conectado a SQL Server. El endpoint `GET /productos/opciones` carga las categorías y empleados existentes para respetar sus claves foráneas.

El alta corresponde al esquema de `Productos`: `ProductoId` y las fechas se generan en SQL Server; se seleccionan `CategoriaId` y `EmpleadoId` existentes, y se ingresan nombre, slug, costo, URL de imagen y existencias. Descripción es opcional y destacado es una casilla. Hibernate valida el esquema al iniciar, sin alterarlo.

La app consulta los registros guardados con `GET /productos`, carga las opciones de categoría/empleado con `GET /productos/opciones` y guarda productos con `POST /productos`.

## Requisitos

- Node.js 20 o posterior
- Java 17

## Ejecutar en desarrollo (Windows)

Antes de arrancar la aplicacion por primera vez, copia `application-local.properties.example` a `application-local.properties` en la raiz y completa los datos de conexion a SQL Server. Ese archivo local esta excluido de Git.

La conexion usa autenticacion integrada de Windows con la instancia `AMD-GIGABYTE-1T\SQLEXPRESS` y la base `Inventario`. La base debe existir. Para esta autenticacion, descarga el paquete oficial del Microsoft JDBC Driver que coincida con la version indicada por Gradle y coloca `mssql-jdbc_auth-<version>.x64.dll` en la carpeta `drivers` de la raiz del proyecto. La carpeta se crea al extraer el archivo; las DLL estan excluidas de Git. La version actualmente resuelta requiere `mssql-jdbc_auth-13.4.0.x64.dll`.

Abre dos terminales desde la raiz del repositorio:

1. Inicia el backend: `.\gradlew.bat bootRun`
2. Inicia el escritorio:

   ```powershell
   Set-Location desktop
   npm install
   npm run dev
   ```

El backend usa por defecto `http://localhost:8080`. Si utiliza otra direccion, define `INVENTARIO_API_URL` antes de iniciar Electron, por ejemplo:

```powershell
$env:INVENTARIO_API_URL = "http://localhost:8081"
npm run dev
```

## Compilar la interfaz

Desde `desktop`, ejecuta `npm run build` y luego `npm start`. Para iniciar la aplicacion compilada, el backend Spring Boot debe estar activo.

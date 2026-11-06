package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.infrastructure.InventarioApplication;
import com.example.inventario.infrastructure.adapter.out.persistence.CategoriaEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.ColorEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.CredencialUsuarioEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.DocumentoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.EmpleadoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.InsumoMenorEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataCategoriaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataColorRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataCredencialUsuarioRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataDocumentoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataDetallePedidoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataEmpleadoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataInsumoMenorRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataPedidoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataProductoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataProductoTallaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.RolEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataRolRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataTallaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = InventarioApplication.class)
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrarProductoUseCase registrarProducto;

    @Autowired
    private SpringDataCategoriaRepository categoriaRepository;

    @Autowired
    private SpringDataEmpleadoRepository empleadoRepository;

    @Autowired
    private SpringDataRolRepository rolRepository;

    @Autowired
    private SpringDataProductoRepository productoRepository;

    @Autowired
    private SpringDataDocumentoRepository documentoRepository;

    @Autowired
    private SpringDataColorRepository colorRepository;

    @Autowired
    private SpringDataTallaRepository tallaRepository;

    @Autowired
    private SpringDataProductoTallaRepository productoTallaRepository;

    @Autowired
    private SpringDataPedidoRepository pedidoRepository;

    @Autowired
    private SpringDataDetallePedidoRepository detallePedidoRepository;

    @Autowired
    private SpringDataUsuarioRepository usuarioRepository;

    @Autowired
    private SpringDataCredencialUsuarioRepository credencialUsuarioRepository;

    @Autowired
    private SpringDataInsumoMenorRepository insumoMenorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int categoriaId;
    private int ciudadId;

    @BeforeEach
    void prepararCatalogo() {
        colorRepository.deleteAll();
        productoTallaRepository.deleteAll();
        tallaRepository.deleteAll();
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        empleadoRepository.deleteAll();
        rolRepository.deleteAll();
        credencialUsuarioRepository.deleteAll();
        insumoMenorRepository.deleteAll();
        usuarioRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM Ciudades");
        documentoRepository.deleteAll();

        DocumentoEntity documento = documentoRepository.save(new DocumentoEntity("Cédula"));
        jdbcTemplate.update("INSERT INTO Ciudades (Ciudad) VALUES (?)", "Bogotá");
        ciudadId = jdbcTemplate.queryForObject("SELECT MAX(CiudadId) FROM Ciudades", Integer.class);
        jdbcTemplate.update("""
                INSERT INTO Usuarios
                    (UsuarioId, DocumentoId, CiudadId, Nombre, Direccion, Telefono, Correo, FechaNacimiento)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, 77, documento.getId(), ciudadId, "Cliente Prueba", "Calle 1", "3001234567",
                "cliente@inventario.test", java.sql.Date.valueOf("2000-01-01"));
        categoriaId = categoriaRepository.save(new CategoriaEntity(
                "Repostería", "reposteria", null, "https://inventario.test/reposteria.jpg"
        )).getId();
        RolEntity role = rolRepository.save(new RolEntity("Inventario"));
        empleadoRepository.save(new EmpleadoEntity(
                25, documento.getId(), "Ana López", role, "3001112222", "ana@inventario.test"
        ));
    }

    @Test
    void readsCatalogsAndCreatesAndListsProductsThroughApi() throws Exception {
        mockMvc.perform(get("/productos/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].id").value(categoriaId))
                .andExpect(jsonPath("$.empleados", hasSize(1)))
                .andExpect(jsonPath("$.empleados[0].id").value(25));

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Churro clásico",
                                  "slug": "churro-clasico",
                                  "descripcion": "Churro con azúcar",
                                  "costo": 12.50,
                                  "imagenUrl": "https://inventario.test/churro.jpg",
                                  "existencias": 10,
                                  "destacado": true
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.empleadoId").value(25))
                .andExpect(jsonPath("$.costo").value(12.50));

        assertEquals(1, productoRepository.count());
        assertTrue(registrarProducto.listar().get(0).id() > 0);

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Churro clásico"))
                .andExpect(jsonPath("$[0].existencias").value(10));
    }

    @Test
    void allowsProductsWithRepeatedNames() throws Exception {
        String product = """
                {
                  "categoriaId": %d,
                  "empleadoId": 25,
                  "nombre": "Producto repetido",
                  "slug": "%s",
                  "costo": 8.00,
                  "imagenUrl": "https://inventario.test/%s.jpg",
                  "existencias": 0,
                  "destacado": false
                }
                """;

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(product.formatted(categoriaId, "producto-repetido-1", "producto-1")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(product.formatted(categoriaId, "producto-repetido-2", "producto-2")))
                .andExpect(status().isCreated());

        assertEquals(2, productoRepository.count());
    }

    @Test
    void rejectsMissingRequiredFieldsAndUnknownForeignKeys() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "",
                                  "slug": "",
                                  "costo": null,
                                  "imagenUrl": "",
                                  "existencias": 0,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": 999999,
                                  "empleadoId": 25,
                                  "nombre": "Producto",
                                  "slug": "producto",
                                  "costo": 8.00,
                                  "imagenUrl": "https://inventario.test/producto.jpg",
                                  "existencias": 0,
                                  "destacado": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La categoría seleccionada no existe."));
    }

    @Test
    void createsAndListsCategoriesUsingRequiredDatabaseColumns() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Accesorios",
                                  "slug": "accesorios",
                                  "descripcion": "Complementos",
                                  "imagenUrl": "https://inventario.test/accesorios.jpg",
                                  "estado": "Inactivo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Accesorios"))
                .andExpect(jsonPath("$.slug").value("accesorios"))
                .andExpect(jsonPath("$.estado").value("Inactivo"));

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "accesorios",
                                  "slug": "accesorios-2",
                                  "imagenUrl": "https://inventario.test/accesorios-2.jpg"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe una categoría con ese nombre."));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/productos/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].id").value(categoriaId));
    }

    @Test
    void rejectsUnsupportedCategoryState() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pijamas",
                                  "slug": "pijamas",
                                  "imagenUrl": "https://inventario.test/pijamas.jpg",
                                  "estado": "Archivada"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El estado debe ser Activo o Inactivo."));
        assertEquals(1, categoriaRepository.count());
    }

    @Test
    void createsAndListsColorsWithHexCode() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Pijama colorida",
                                  "slug": "pijama-colorida",
                                  "costo": 25.00,
                                  "imagenUrl": "https://inventario.test/pijama.jpg",
                                  "existencias": 3,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        int productoId = productoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/colores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": %d,
                                  "nombre": "Azul océano",
                                  "codigoHex": "#243B67"
                                }
                                """.formatted(productoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Azul océano"))
                .andExpect(jsonPath("$.codigoHex").value("#243B67"));

        mockMvc.perform(get("/colores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoHex").value("#243B67"));
        mockMvc.perform(get("/productos/{id}/colores", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoHex").value("#243B67"));
    }

    @Test
    void rejectsInvalidHexCodeForColor() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Producto de prueba",
                                  "slug": "producto-prueba",
                                  "costo": 10.00,
                                  "imagenUrl": "https://inventario.test/producto.jpg",
                                  "existencias": 1,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        int productoId = productoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/colores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productoId": %d,
                                  "nombre": "Azul",
                                  "codigoHex": "blue"
                                }
                                """.formatted(productoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "El código del color debe tener formato hexadecimal, por ejemplo #14B8A6."
                ));
        assertEquals(0, colorRepository.count());
    }

    @Test
    void updatesCategoryStateIndependentlyAndOnlyActiveCategoriesAreProductOptions() throws Exception {
        int inactiveCategoryId = categoriaRepository.save(new CategoriaEntity(
                "Accesorios", "accesorios", null, "https://inventario.test/accesorios.jpg", "Inactivo"
        )).getId();

        mockMvc.perform(get("/productos/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].id").value(categoriaId));

        mockMvc.perform(put("/categorias/{id}/estado", inactiveCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"Activo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inactiveCategoryId))
                .andExpect(jsonPath("$.estado").value("Activo"));

        mockMvc.perform(put("/categorias/{id}/estado", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"Inactivo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Inactivo"));

        mockMvc.perform(get("/productos/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(1)))
                .andExpect(jsonPath("$.categorias[0].id").value(inactiveCategoryId));

        assertEquals("Activo", categoriaRepository.findById(inactiveCategoryId).orElseThrow().getEstado());
        assertEquals("Inactivo", categoriaRepository.findById(categoriaId).orElseThrow().getEstado());
    }

    @Test
    void createsEmployeesWithFieldsPresentInTheExistingSchema() throws Exception {
        mockMvc.perform(post("/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 26,
                                  "documentoId": %d,
                                  "nombre": "Luis Ventas",
                                  "rolId": %d,
                                  "estado": "Activo",
                                  "telefono": "3003334444",
                                  "correo": "luis@inventario.test",
                                  "contrasena": "ClaveTemporal123"
                                }
                                """.formatted(documentoRepository.findAll().get(0).getId(),
                                rolRepository.save(new RolEntity("Ventas")).getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(26))
                .andExpect(jsonPath("$.rol").value("Ventas"));

        mockMvc.perform(post("/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 26,
                                  "documentoId": 999,
                                  "nombre": "Duplicado",
                                  "rolId": 1,
                                  "estado": "Activo",
                                  "correo": "duplicado@inventario.test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El tipo de documento seleccionado no existe."));

        mockMvc.perform(get("/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        EmpleadoEntity saved = empleadoRepository.findById(26).orElseThrow();
        assertTrue(saved.getContrasenaHash().startsWith("pbkdf2-sha256$"));
        assertFalse(saved.getContrasenaHash().contains("ClaveTemporal123"));
        assertEquals("Activo", saved.getEstado());
    }

    @Test
    void listsRolesFromTheDatabaseForEmployeeSelection() throws Exception {
        rolRepository.save(new RolEntity("Ventas"));
        mockMvc.perform(get("/roles/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Inventario"))
                .andExpect(jsonPath("$[1].nombre").value("Ventas"));
    }

    @Test
    void createsAnEmptySaleForAnExistingUserAndRejectsUnknownUsers() throws Exception {
        mockMvc.perform(post("/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":77}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(77))
                .andExpect(jsonPath("$.estado").value("Generado"));

        mockMvc.perform(post("/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El usuario seleccionado no existe."));

        mockMvc.perform(get("/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createsSaleWithoutCustomerWhenNoneIsSelected() throws Exception {
        mockMvc.perform(post("/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":null}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.estado").value("Generado"));
    }

    @Test
    void createsUsersUsingOnlyColumnsFromTheDatabase() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 123456,
                                  "documentoId": %d,
                                  "ciudadId": %d,
                                  "nombre": "Cliente Nuevo",
                                  "direccion": "Calle 2",
                                  "telefono": "3005556677",
                                  "correo": "nuevo@inventario.test",
                                  "fechaNacimiento": "1998-02-14",
                                  "contrasena": "ClaveCliente123",
                                  "confirmarContrasena": "ClaveCliente123"
                                }
                                """.formatted(documentoRepository.findAll().get(0).getId(), ciudadId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123456))
                .andExpect(jsonPath("$.nombre").value("Cliente Nuevo"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist());
        CredencialUsuarioEntity credential = credencialUsuarioRepository.findById(123456).orElseThrow();
        assertTrue(credential.getContrasenaHash().startsWith("pbkdf2-sha256$"));
        assertFalse(credential.getContrasenaHash().contains("ClaveCliente123"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/usuarios/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(123456));

        mockMvc.perform(get("/usuarios/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentos", hasSize(1)))
                .andExpect(jsonPath("$.ciudades", hasSize(1)));
    }

    @Test
    void rejectsCustomerWhenPasswordsDoNotMatchWithoutSavingEitherRecord() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 123456,
                                  "documentoId": %d,
                                  "ciudadId": %d,
                                  "nombre": "Cliente Nuevo",
                                  "direccion": "Calle 2",
                                  "telefono": "3005556677",
                                  "correo": "nuevo@inventario.test",
                                  "fechaNacimiento": "1998-02-14",
                                  "contrasena": "ClaveCliente123",
                                  "confirmarContrasena": "OtraClave123"
                                }
                                """.formatted(documentoRepository.findAll().get(0).getId(), ciudadId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Las contraseñas no coinciden."));
        assertEquals(1, usuarioRepository.count());
        assertEquals(0, credencialUsuarioRepository.count());
    }

    @Test
    void createsListsAndRejectsDuplicateMinorSuppliesByReference() throws Exception {
        String supply = """
                {
                  "nombre": "Hilo de algodón",
                  "referencia": "%s",
                  "unidadMedida": "cono",
                  "costoUnitario": 12500.50,
                  "cantidadDisponible": 4
                }
                """;
        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supply.formatted("HILO-001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referencia").value("HILO-001"))
                .andExpect(jsonPath("$.unidadMedida").value("cono"))
                .andExpect(jsonPath("$.cantidadDisponible").value(4));

        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supply.formatted("hilo-001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe un insumo con esa referencia."));

        mockMvc.perform(get("/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        assertEquals(1, insumoMenorRepository.count());
    }

    @Test
    void rejectsMissingOrNegativeMinorSupplyValues() throws Exception {
        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Botón",
                                  "referencia": "BOT-001",
                                  "unidadMedida": "",
                                  "costoUnitario": -1,
                                  "cantidadDisponible": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La unidad de medida es obligatoria."));

        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Botón",
                                  "referencia": "BOT-002",
                                  "unidadMedida": "Unidad inventada",
                                  "costoUnitario": 10,
                                  "cantidadDisponible": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La unidad de medida seleccionada no es válida."));
        assertEquals(0, insumoMenorRepository.count());
    }

    @Test
    void productionIngressIncreasesExistingProductStock() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Producto producción",
                                  "slug": "producto-produccion",
                                  "costo": 12.50,
                                  "imagenUrl": "https://inventario.test/produccion.jpg",
                                  "existencias": 6,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        int productoId = productoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/produccion/ingresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":" + productoId + ",\"cantidad\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadIngresada").value(4))
                .andExpect(jsonPath("$.existenciasActuales").value(10));

        assertEquals(10, productoRepository.findById(productoId).orElseThrow().getExistencias());
    }

    @Test
    void addsSaleItemsAndAccumulatesRepeatedProductWithoutReducingInventory() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Producto para comanda",
                                  "slug": "producto-comanda",
                                  "costo": 12.50,
                                  "imagenUrl": "https://inventario.test/comanda.jpg",
                                  "existencias": 9,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        int productoId = productoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":77}"))
                .andExpect(status().isCreated());
        int pedidoId = pedidoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/ventas/{pedidoId}/productos", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":" + productoId + ",\"cantidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].costo").value(12.50))
                .andExpect(jsonPath("$.items[0].subtotal").value(25.00))
                .andExpect(jsonPath("$.total").value(25.00));

        mockMvc.perform(post("/ventas/{pedidoId}/productos", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":" + productoId + ",\"cantidad\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].cantidad").value(5))
                .andExpect(jsonPath("$.items[0].subtotal").value(62.50))
                .andExpect(jsonPath("$.total").value(62.50));

        mockMvc.perform(get("/ventas/{pedidoId}/productos", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(62.50));

        mockMvc.perform(put("/ventas/{pedidoId}/productos/{productoId}", pedidoId, productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(4))
                .andExpect(jsonPath("$.total").value(50.00));

        mockMvc.perform(delete("/ventas/{pedidoId}/productos/{productoId}", pedidoId, productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
        assertEquals(9, productoRepository.findById(productoId).orElseThrow().toDomain().existencias());
        assertEquals(0, detallePedidoRepository.count());
    }

    @Test
    void rejectsInvalidOrderItemAndMissingOrder() throws Exception {
        mockMvc.perform(post("/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":77}"))
                .andExpect(status().isCreated());
        int pedidoId = pedidoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/ventas/{pedidoId}/productos", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La cantidad debe ser un entero mayor que cero."));

        mockMvc.perform(post("/ventas/{pedidoId}/productos", pedidoId + 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La venta seleccionada no existe."));
    }

    @Test
    void createsProductSpecificColorsAndSizesAndRejectsDuplicates() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoriaId": %d,
                                  "empleadoId": 25,
                                  "nombre": "Producto variantes",
                                  "slug": "producto-variantes",
                                  "costo": 8.00,
                                  "imagenUrl": "https://inventario.test/producto.jpg",
                                  "existencias": 0,
                                  "destacado": false
                                }
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated());
        int productoId = productoRepository.findAll().get(0).getId();

        String color = "{\"productoId\":" + productoId + ",\"nombre\":\"Azul\"}";
        mockMvc.perform(post("/colores").contentType(MediaType.APPLICATION_JSON).content(color))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Azul"));
        mockMvc.perform(post("/colores").contentType(MediaType.APPLICATION_JSON).content(color))
                .andExpect(status().isBadRequest());

        String talla = """
                {
                  "codigo": "M",
                  "nombre": "Mediana",
                  "sistema": "Adulto",
                  "ordenVisual": 3,
                  "estado": "Activo"
                }
                """;
        mockMvc.perform(post("/tallas").contentType(MediaType.APPLICATION_JSON).content(talla))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Mediana"))
                .andExpect(jsonPath("$.codigo").value("M"))
                .andExpect(jsonPath("$.ordenVisual").value(3));
        mockMvc.perform(post("/tallas").contentType(MediaType.APPLICATION_JSON).content(talla))
                .andExpect(status().isBadRequest());
        int tallaId = tallaRepository.findAllByOrderBySistemaAscOrdenVisualAscNombreDescriptivoAsc()
                .get(0).getId();
        mockMvc.perform(post("/productos/{productoId}/tallas/{tallaId}", productoId, tallaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos").value(1));
        mockMvc.perform(post("/productos/{productoId}/tallas/{tallaId}", productoId, tallaId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/productos/{productoId}/colores", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/productos/{productoId}/tallas", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value("M"));
        mockMvc.perform(get("/tallas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productos").value(1));
    }
}

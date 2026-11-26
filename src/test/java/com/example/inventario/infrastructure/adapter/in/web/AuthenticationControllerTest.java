package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.infrastructure.InventarioApplication;
import com.example.inventario.infrastructure.adapter.out.persistence.DocumentoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.EmpleadoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.RolEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataDocumentoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataEmpleadoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = InventarioApplication.class, properties = {
        "inventario.auth.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:InventarioAuthTestdb"
})
@AutoConfigureMockMvc
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataEmpleadoRepository empleadoRepository;

    @Autowired
    private SpringDataRolRepository rolRepository;

    @Autowired
    private SpringDataDocumentoRepository documentoRepository;

    @BeforeEach
    void setUp() {
        empleadoRepository.deleteAll();
        rolRepository.deleteAll();
        documentoRepository.deleteAll();
        DocumentoEntity documento = documentoRepository.save(new DocumentoEntity("Cédula"));
        RolEntity jefe = rolRepository.save(new RolEntity("Jefe"));
        empleadoRepository.save(new EmpleadoEntity(
                12345, documento.getId(), "Jefa de prueba", jefe, null, "jefa@test.local",
                PasswordHasher.hash("ClaveSegura123"), "Activo"
        ));
        RolEntity ventas = rolRepository.save(new RolEntity("Empleado de ventas"));
        empleadoRepository.save(new EmpleadoEntity(
                12346, documento.getId(), "Vendedor de prueba", ventas, null, "ventas@test.local",
                PasswordHasher.hash("ClaveSegura123"), "Activo"
        ));
        RolEntity production = rolRepository.save(new RolEntity("Empleado de producción"));
        empleadoRepository.save(new EmpleadoEntity(
                12347, documento.getId(), "Productor de prueba", production, null, "produccion@test.local",
                PasswordHasher.hash("ClaveSegura123"), "Activo"
        ));
        RolEntity administrator = rolRepository.save(new RolEntity("Administrador"));
        empleadoRepository.save(new EmpleadoEntity(
                12348, documento.getId(), "Administrador de prueba", administrator, null, "admin@test.local",
                PasswordHasher.hash("ClaveSegura123"), "Activo"
        ));
        RolEntity inventory = rolRepository.save(new RolEntity("Inventario"));
        empleadoRepository.save(new EmpleadoEntity(
                12349, documento.getId(), "Empleado no autorizado", inventory, null, "inventario@test.local",
                PasswordHasher.hash("ClaveSegura123"), "Activo"
        ));
    }

    @Test
    void bossCanLogInAndAccessAdministrationAndProfitability() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isUnauthorized());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empleadoId":12345,"contrasena":"ClaveSegura123"}
                                """))
                .andReturn();
        String response = loginResult.getResponse().getContentAsString();
        assertEquals(200, loginResult.getResponse().getStatus(), response);
        assertTrue(response.contains("\"id\":12345"));
        assertTrue(response.contains("\"rol\":\"Jefe\""));
        Matcher tokenMatcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(response);
        if (!tokenMatcher.find()) {
            throw new AssertionError("Login response did not contain a token");
        }
        String token = tokenMatcher.group(1);

        mockMvc.perform(get("/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/reportes/rentabilidad").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsIncorrectCredentialsAndUnknownRoles() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empleadoId":12345,"contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {"empleadoId":12349,"contrasena":"ClaveSegura123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deactivatedEmployeeCanNoLongerLogIn() throws Exception {
        EmpleadoEntity employee = empleadoRepository.findById(12345).orElseThrow();
        employee.desactivar();
        empleadoRepository.save(employee);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empleadoId":12345,"contrasena":"ClaveSegura123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bossSalesAndProductionEmployeesCanChangeTheirOwnPassword() throws Exception {
        String newPassword = "NuevaClave456!";
        String changeRequest = """
                {
                  "currentPassword": "ClaveSegura123",
                  "newPassword": "%s",
                  "confirmPassword": "%s"
                }
                """.formatted(newPassword, newPassword);

        for (int employeeId : new int[]{12345, 12346, 12347}) {
            String authorization = "Bearer " + loginToken(employeeId);
            mockMvc.perform(put("/auth/contrasena")
                            .header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeRequest))
                    .andExpect(status().isNoContent());

            String storedHash = empleadoRepository.findById(employeeId).orElseThrow().getContrasenaHash();
            assertTrue(PasswordHasher.matches(newPassword, storedHash));

            mockMvc.perform(post("/auth/logout").header("Authorization", authorization))
                    .andExpect(status().isNoContent());
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"empleadoId":%d,"contrasena":"%s"}
                                    """.formatted(employeeId, newPassword)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rejectsIncorrectCurrentPasswordOrConfirmationMismatch() throws Exception {
        String authorization = "Bearer " + loginToken(12346);
        String storedHash = empleadoRepository.findById(12346).orElseThrow().getContrasenaHash();

        mockMvc.perform(put("/auth/contrasena")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Incorrecta123",
                                  "newPassword": "NuevaClave456!",
                                  "confirmPassword": "NuevaClave456!"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/auth/contrasena")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "ClaveSegura123",
                                  "newPassword": "NuevaClave456!",
                                  "confirmPassword": "OtraClave789!"
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertTrue(PasswordHasher.matches("ClaveSegura123", storedHash));
    }

    @Test
    void salesRoleCanUseSalesRoutesButNotAdministrationRoutes() throws Exception {
        String token = loginToken(12346);
        String authorization = "Bearer " + token;

        mockMvc.perform(get("/ventas").header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(get("/usuarios/opciones").header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(get("/productos/99999/variantes").header("Authorization", authorization))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/ventas/99999/registrar")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"asignaciones\":[]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/empleados").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/empleados/{empleadoId}/estado", 12346)
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/productos").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/auth/logout").header("Authorization", authorization))
                .andExpect(status().isNoContent());
    }

    @Test
    void productionRoleCanUseProductsAndProductionRoutesOnly() throws Exception {
        String authorization = "Bearer " + loginToken(12347);

        mockMvc.perform(get("/productos").header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(get("/productos/opciones").header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(get("/productos/99999/variantes").header("Authorization", authorization))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/produccion/ingresos")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":99999,\"cantidad\":1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/productos/99999/existencias").header("Authorization", authorization))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/ventas").header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/ventas/99999/registrar")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"asignaciones\":[]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/empleados").header("Authorization", authorization))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorHasFullAccessExceptTheBossOnlyProfitabilityReport() throws Exception {
        String authorization = "Bearer " + loginToken(12348);

        mockMvc.perform(get("/empleados").header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(put("/empleados/{empleadoId}/estado", 12349)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Inactivo"));
        mockMvc.perform(get("/reportes/rentabilidad").header("Authorization", authorization))
                .andExpect(status().isForbidden());
    }

    @Test
    void upgradesLegacyPlaintextPasswordToPbkdf2OnSuccessfulLogin() throws Exception {
        EmpleadoEntity admin = empleadoRepository.findById(12345).orElseThrow();
        admin.setContrasenaHash("ClaveAntigua123!");
        empleadoRepository.save(admin);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empleadoId":12345,"contrasena":"ClaveAntigua123!"}
                                """))
                .andExpect(status().isOk());

        String migratedHash = empleadoRepository.findById(12345).orElseThrow().getContrasenaHash();
        assertTrue(migratedHash.startsWith("pbkdf2-sha256$"));
        assertTrue(PasswordHasher.matches("ClaveAntigua123!", migratedHash));
    }

    private String loginToken(int employeeId) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empleadoId":%d,"contrasena":"ClaveSegura123"}
                                """.formatted(employeeId)))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        assertEquals(200, result.getResponse().getStatus(), response);
        Matcher matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(response);
        if (!matcher.find()) {
            throw new AssertionError("Login response did not contain a token");
        }
        return matcher.group(1);
    }
}

package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.infrastructure.adapter.out.persistence.EmpleadoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataEmpleadoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private static final String INVALID_CREDENTIALS = "Credenciales inválidas o cuenta sin permisos de acceso.";
    private final SpringDataEmpleadoRepository empleadoRepository;
    private final AuthenticationSessionRegistry sessionRegistry;

    public AuthenticationController(
            SpringDataEmpleadoRepository empleadoRepository,
            AuthenticationSessionRegistry sessionRegistry
    ) {
        this.empleadoRepository = empleadoRepository;
        this.sessionRegistry = sessionRegistry;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.empleadoId() <= 0 || request.contrasena() == null || request.contrasena().isBlank()) {
            return unauthorized();
        }
        EmpleadoEntity empleado = empleadoRepository.findById(request.empleadoId()).orElse(null);
        if (empleado == null
                || !isActive(empleado.getEstado())
                || !isKnownRole(empleado.getRol())
        ) {
            return unauthorized();
        }

        String storedPassword = empleado.getContrasenaHash();
        if (!PasswordHasher.matches(request.contrasena(), storedPassword)) {
            if (!PasswordHasher.matchesLegacyPlaintext(request.contrasena(), storedPassword)) {
                return unauthorized();
            }
            empleado.setContrasenaHash(PasswordHasher.hash(request.contrasena()));
            empleadoRepository.save(empleado);
        }

        AuthenticationSessionRegistry.Session session = sessionRegistry.create(empleado.getId());
        return ResponseEntity.ok(new LoginResponse(
                session.token(),
                session.expiresAt(),
                new EmployeeProfile(empleado.getId(), empleado.getNombre(), empleado.getRol().trim())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        sessionRegistry.revoke(bearerToken(authorization));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/contrasena")
    public ResponseEntity<?> changePassword(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Completa los datos de cambio de contraseña.");
        }
        AuthenticationSessionRegistry.Session session = sessionRegistry.find(bearerToken(authorization));
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        EmpleadoEntity employee = empleadoRepository.findById(session.employeeId()).orElse(null);
        if (employee == null || !isActive(employee.getEstado())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentPassword = request.currentPassword();
        String newPassword = request.newPassword();
        if (!PasswordHasher.matches(currentPassword, employee.getContrasenaHash())
                && !PasswordHasher.matchesLegacyPlaintext(currentPassword, employee.getContrasenaHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 128) {
            throw new IllegalArgumentException("La nueva contraseña debe tener entre 8 y 128 caracteres.");
        }
        if (!newPassword.equals(request.confirmPassword())) {
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña.");
        }
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }

        employee.setContrasenaHash(PasswordHasher.hash(newPassword));
        empleadoRepository.save(employee);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordChange(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    static boolean isKnownRole(String role) {
        return roleCategory(role) != null;
    }

    static boolean canAccess(String role, String method, String path) {
        String category = roleCategory(role);
        if (category == null) {
            return false;
        }
        if ("boss".equals(category)) {
            return true;
        }
        if ("administrator".equals(category)) {
            return !"/reportes/rentabilidad".equals(path);
        }

        String normalizedMethod = method.toUpperCase(Locale.ROOT);
        if ("PUT".equals(normalizedMethod) && "/auth/contrasena".equals(path)) {
            return true;
        }
        if ("POST".equals(normalizedMethod) && "/auth/logout".equals(path)) {
            return true;
        }
        if ("sales".equals(category)) {
            return ("GET".equals(normalizedMethod) && (
                    "/usuarios/opciones".equals(path)
                            || "/ventas".equals(path)
                            || "/ventas/productos".equals(path)
                            || Pattern.matches("/ventas/\\d+/productos", path)
                            || Pattern.matches("/productos/\\d+/variantes", path)
            )) || ("POST".equals(normalizedMethod) && (
                    "/ventas".equals(path)
                            || Pattern.matches("/ventas/\\d+/productos", path)
                            || Pattern.matches("/ventas/\\d+/registrar", path)
            )) || (("PUT".equals(normalizedMethod) || "DELETE".equals(normalizedMethod))
                    && Pattern.matches("/ventas/\\d+/productos/\\d+", path));
        }
        if ("production".equals(category)) {
            return ("GET".equals(normalizedMethod)
                    && ("/productos".equals(path) || "/productos/opciones".equals(path)
                    || Pattern.matches("/productos/\\d+/variantes", path)))
                    || ("POST".equals(normalizedMethod)
                    && ("/productos".equals(path) || "/produccion/ingresos".equals(path)))
                    || ("DELETE".equals(normalizedMethod)
                    && Pattern.matches("/productos/\\d+/existencias", path));
        }
        return false;
    }

    private static String roleCategory(String role) {
        if (role == null) {
            return null;
        }
        String normalized = Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        if (normalized.startsWith("empleado de ")) {
            normalized = normalized.substring("empleado de ".length());
        }
        return switch (normalized) {
            case "jefe" -> "boss";
            case "administrador", "administracion" -> "administrator";
            case "ventas" -> "sales";
            case "produccion" -> "production";
            default -> null;
        };
    }

    static boolean isActive(String status) {
        return status != null && status.trim().equalsIgnoreCase("Activo");
    }

    static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private static ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(INVALID_CREDENTIALS));
    }

    public record LoginRequest(int empleadoId, String contrasena) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {}
    public record EmployeeProfile(Integer id, String nombre, String rol) {}
    public record LoginResponse(String token, Instant expiresAt, EmployeeProfile empleado) {}
    public record ErrorResponse(String error) {}
}

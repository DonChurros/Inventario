package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.infrastructure.adapter.out.persistence.EmpleadoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataEmpleadoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "inventario.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationSessionRegistry sessionRegistry;
    private final SpringDataEmpleadoRepository empleadoRepository;

    public AuthenticationFilter(
            AuthenticationSessionRegistry sessionRegistry,
            SpringDataEmpleadoRepository empleadoRepository
    ) {
        this.sessionRegistry = sessionRegistry;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/auth/login".equals(request.getRequestURI().substring(request.getContextPath().length()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = AuthenticationController.bearerToken(request.getHeader("Authorization"));
        AuthenticationSessionRegistry.Session session = sessionRegistry.find(token);
        EmpleadoEntity employee = session == null
                ? null
                : empleadoRepository.findById(session.employeeId()).orElse(null);
        if (employee == null || !AuthenticationController.isActive(employee.getEstado())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Sesión inválida o expirada. Inicia sesión nuevamente.\"}");
            return;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!AuthenticationController.canAccess(employee.getRol(), request.getMethod(), path)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Tu rol no tiene permiso para realizar esta acción.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}

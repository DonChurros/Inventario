package com.example.inventario.infrastructure.adapter.in.web;

import com.example.inventario.domain.model.OpcionCatalogo;
import com.example.inventario.infrastructure.adapter.out.persistence.CategoriaEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.CiudadEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.ColorEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.CredencialUsuarioEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.DetallePedidoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.DetallePedidoId;
import com.example.inventario.infrastructure.adapter.out.persistence.EmpleadoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.InsumoMenorEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.PedidoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.ProductoEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.ProductoTallaEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.RolEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataCategoriaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataCiudadRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataColorRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataCredencialUsuarioRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataDetallePedidoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataDocumentoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataEmpleadoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataInsumoMenorRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataPedidoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataProductoRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataProductoTallaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataRolRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataTallaRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.SpringDataUsuarioRepository;
import com.example.inventario.infrastructure.adapter.out.persistence.TallaEntity;
import com.example.inventario.infrastructure.adapter.out.persistence.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
public class AdministracionController {
    private static final Set<String> UNIDADES_INSUMO = Set.of(
            "Carrete", "Cono", "Unidad", "Metro", "Paquete", "Rollo",
            "Par", "Caja", "Bolsa", "Docena", "Gramo", "Kilogramo", "Litro"
    );

    private final SpringDataCategoriaRepository categoriaRepository;
    private final SpringDataEmpleadoRepository empleadoRepository;
    private final SpringDataRolRepository rolRepository;
    private final SpringDataDocumentoRepository documentoRepository;
    private final SpringDataProductoRepository productoRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final SpringDataCredencialUsuarioRepository credencialUsuarioRepository;
    private final SpringDataInsumoMenorRepository insumoMenorRepository;
    private final SpringDataCiudadRepository ciudadRepository;
    private final SpringDataPedidoRepository pedidoRepository;
    private final SpringDataDetallePedidoRepository detallePedidoRepository;
    private final SpringDataColorRepository colorRepository;
    private final SpringDataTallaRepository tallaRepository;
    private final SpringDataProductoTallaRepository productoTallaRepository;

    public AdministracionController(
            SpringDataCategoriaRepository categoriaRepository,
            SpringDataEmpleadoRepository empleadoRepository,
            SpringDataRolRepository rolRepository,
            SpringDataDocumentoRepository documentoRepository,
            SpringDataCiudadRepository ciudadRepository,
            SpringDataProductoRepository productoRepository,
            SpringDataUsuarioRepository usuarioRepository,
            SpringDataCredencialUsuarioRepository credencialUsuarioRepository,
            SpringDataInsumoMenorRepository insumoMenorRepository,
            SpringDataPedidoRepository pedidoRepository,
            SpringDataDetallePedidoRepository detallePedidoRepository,
            SpringDataColorRepository colorRepository,
            SpringDataTallaRepository tallaRepository,
            SpringDataProductoTallaRepository productoTallaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.documentoRepository = documentoRepository;
        this.ciudadRepository = ciudadRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.credencialUsuarioRepository = credencialUsuarioRepository;
        this.insumoMenorRepository = insumoMenorRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.colorRepository = colorRepository;
        this.tallaRepository = tallaRepository;
        this.productoTallaRepository = productoTallaRepository;
    }

    private static void ensurePending(PedidoEntity pedido) {
        if (!"Generado".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden editar ventas pendientes.");
        }
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaResponse> crearCategoria(@RequestBody CategoriaRequest request) {
        String nombre = required(request.nombre(), "El nombre de la categoría es obligatorio.");
        String slug = required(request.slug(), "El slug de la categoría es obligatorio.");
        String imagenUrl = required(request.imagenUrl(), "La URL de imagen de la categoría es obligatoria.");
        validateLength(nombre, 100, "El nombre de la categoría admite máximo 100 caracteres.");
        validateLength(slug, 250, "El slug de la categoría admite máximo 250 caracteres.");
        validateLength(imagenUrl, 255, "La URL de imagen admite máximo 255 caracteres.");
        String estado = request.estado() == null || request.estado().isBlank()
                ? "Activo" : request.estado().trim();
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        CategoriaEntity saved = categoriaRepository.save(new CategoriaEntity(
                nombre, slug, blankToNull(request.descripcion()), imagenUrl,
                estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo"
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/categorias/{id}/estado")
    public CategoriaResponse actualizarEstadoCategoria(
            @PathVariable int id,
            @RequestBody CategoriaEstadoRequest request
    ) {
        String estado = request.estado() == null ? "" : request.estado().trim();
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));
        categoria.setEstado(estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo");
        return toResponse(categoriaRepository.save(categoria));
    }

    @GetMapping("/empleados")
    public List<EmpleadoResponse> listarEmpleados() {
        return empleadoRepository.findAllByOrderByNombreAsc().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @GetMapping("/roles/opciones")
    public List<OpcionCatalogo> listarRoles() {
        return rolRepository.findAllByOrderByRolAsc().stream()
                .map(rol -> new OpcionCatalogo(rol.getId(), rol.getRol()))
                .toList();
    }

    @PostMapping("/empleados")
    public ResponseEntity<EmpleadoResponse> crearEmpleado(@RequestBody EmpleadoRequest request) {
        if (request.id() <= 0) {
            throw new IllegalArgumentException("El identificador del empleado debe ser mayor que cero.");
        }
        if (request.documentoId() <= 0 || !documentoRepository.existsById(request.documentoId())) {
            throw new IllegalArgumentException("El tipo de documento seleccionado no existe.");
        }
        String nombre = required(request.nombre(), "El nombre del empleado es obligatorio.");
        String correo = required(request.correo(), "El correo del empleado es obligatorio.");
        String contrasena = required(request.contrasena(), "La contraseña del empleado es obligatoria.");
        String estado = required(request.estado(), "Selecciona el estado del empleado.");
        var rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe."));
        validateLength(nombre, 100, "El nombre del empleado admite máximo 100 caracteres.");
        validateLength(correo, 100, "El correo admite máximo 100 caracteres.");
        if (!estado.equals("Activo") && !estado.equals("Inactivo")) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        if (contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
        if (empleadoRepository.existsById(request.id())) {
            throw new IllegalArgumentException("Ya existe un empleado con ese identificador.");
        }
        if (empleadoRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un empleado con ese correo.");
        }
        EmpleadoEntity saved = empleadoRepository.save(new EmpleadoEntity(
                request.id(), request.documentoId(), nombre, rol,
                blankToNull(request.telefono()), correo, PasswordHasher.hash(contrasena), estado
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/empleados/{empleadoId}/rol")
    public EmpleadoResponse actualizarRolEmpleado(
            @PathVariable int empleadoId,
            @RequestBody ActualizarRolEmpleadoRequest request
    ) {
        EmpleadoEntity empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("El empleado seleccionado no existe."));
        RolEntity rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe."));
        empleado.setRol(rol);
        return toResponse(empleadoRepository.save(empleado));
    }

    @PutMapping("/empleados/{empleadoId}/estado")
    public EmpleadoResponse retirarEmpleado(@PathVariable int empleadoId) {
        EmpleadoEntity empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("El empleado seleccionado no existe."));
        empleado.desactivar();
        return toResponse(empleadoRepository.save(empleado));
    }

    @PutMapping("/empleados/{empleadoId}/contrasena")
    public ResponseEntity<Void> actualizarContrasenaEmpleado(
            @PathVariable int empleadoId,
            @RequestBody ActualizarContrasenaEmpleadoRequest request
    ) {
        EmpleadoEntity empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("El empleado seleccionado no existe."));
        String contrasena = request.contrasena();
        if (contrasena == null || contrasena.isBlank() || contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        empleado.setContrasenaHash(PasswordHasher.hash(contrasena));
        empleadoRepository.save(empleado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/opciones")
    public List<OpcionCatalogo> listarUsuarios() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream()
                .map(usuario -> new OpcionCatalogo(usuario.getId(), usuario.getNombre()))
                .toList();
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuariosCompletos() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @GetMapping("/usuarios/catalogos")
    public UsuarioCatalogOptions opcionesUsuario() {
        return new UsuarioCatalogOptions(
                documentoRepository.findAllByOrderByTipoAsc().stream()
                        .map(documento -> new OpcionCatalogo(documento.getId(), documento.getTipo()))
                        .toList(),
                ciudadRepository.findAllByOrderByCiudadAsc().stream()
                        .map(ciudad -> new OpcionCatalogo(ciudad.getId(), ciudad.getCiudad()))
                        .toList()
        );
    }

    @PostMapping("/usuarios")
    @Transactional
    public ResponseEntity<UsuarioResponse> crearUsuario(@RequestBody UsuarioRequest request) {
        if (request.id() < 100_000 || request.id() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El ID debe tener entre 6 y 10 dígitos y estar dentro del rango de la base.");
        }
        if (request.documentoId() <= 0 || !documentoRepository.existsById(request.documentoId())) {
            throw new IllegalArgumentException("El tipo de documento seleccionado no existe.");
        }
        if (request.ciudadId() <= 0 || !ciudadRepository.existsById(request.ciudadId())) {
            throw new IllegalArgumentException("La ciudad seleccionada no existe.");
        }
        String nombre = required(request.nombre(), "El nombre es obligatorio.");
        String direccion = required(request.direccion(), "La dirección es obligatoria.");
        String telefono = required(request.telefono(), "El teléfono es obligatorio.");
        String correo = required(request.correo(), "El correo es obligatorio.");
        String contrasena = required(request.contrasena(), "La contraseña es obligatoria.");
        String confirmarContrasena = required(
                request.confirmarContrasena(), "Confirma la contraseña."
        );
        if (nombre.length() > 100 || direccion.length() > 150
                || telefono.length() > 20 || correo.length() > 100) {
            throw new IllegalArgumentException("Uno de los campos supera la longitud permitida.");
        }
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
        if (contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!contrasena.equals(confirmarContrasena)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        if (usuarioRepository.existsById((int) request.id())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }
        if (request.fechaNacimiento() == null || request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ingresa una fecha de nacimiento válida.");
        }
        UsuarioEntity saved = usuarioRepository.save(new UsuarioEntity(
                (int) request.id(), request.documentoId(), request.ciudadId(),
                nombre, direccion, telefono, correo, request.fechaNacimiento()
        ));
        credencialUsuarioRepository.save(new CredencialUsuarioEntity(
                saved.getId(), PasswordHasher.hash(contrasena)
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/insumos")
    public List<InsumoMenorResponse> listarInsumos() {
        return insumoMenorRepository.findAllByOrderByNombreAsc().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @PostMapping("/insumos")
    public ResponseEntity<InsumoMenorResponse> crearInsumo(
            @RequestBody InsumoMenorRequest request
    ) {
        String nombre = required(request.nombre(), "El nombre del insumo es obligatorio.");
        String referencia = required(request.referencia(), "La referencia es obligatoria.");
        String unidadMedida = required(request.unidadMedida(), "La unidad de medida es obligatoria.");
        validateLength(nombre, 100, "El nombre admite máximo 100 caracteres.");
        validateLength(referencia, 50, "La referencia admite máximo 50 caracteres.");
        validateLength(unidadMedida, 30, "La unidad de medida admite máximo 30 caracteres.");
        if (UNIDADES_INSUMO.stream().noneMatch(unidad -> unidad.equalsIgnoreCase(unidadMedida))) {
            throw new IllegalArgumentException("La unidad de medida seleccionada no es válida.");
        }
        validateNonNegativeDecimal(request.costoUnitario(), "El costo unitario");
        validateNonNegativeDecimal(request.cantidadDisponible(), "La cantidad disponible");
        if (insumoMenorRepository.existsByReferenciaIgnoreCase(referencia)) {
            throw new IllegalArgumentException("Ya existe un insumo con esa referencia.");
        }
        InsumoMenorEntity saved = insumoMenorRepository.save(new InsumoMenorEntity(
                nombre, referencia, unidadMedida, request.costoUnitario(), request.cantidadDisponible()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/ciudades/opciones")
    public List<OpcionCatalogo> listarCiudades() {
        return ciudadRepository.findAllByOrderByCiudadAsc().stream()
                .map(ciudad -> new OpcionCatalogo(ciudad.getId(), ciudad.getCiudad()))
                .toList();
    }

    @PostMapping("/produccion/ingresos")
    @Transactional
    public ResponseEntity<IngresoProduccionResponse> registrarIngreso(
            @RequestBody IngresoProduccionRequest request
    ) {
        if (request.productoId() <= 0) {
            throw new IllegalArgumentException("Selecciona un producto válido.");
        }
        if (request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad producida debe ser mayor que cero.");
        }
        var producto = productoRepository.findByIdForUpdate(request.productoId())
                .orElseThrow(() -> new IllegalArgumentException("El producto seleccionado no existe."));
        if (request.cantidad() > Integer.MAX_VALUE - producto.getExistencias()) {
            throw new IllegalArgumentException("La cantidad supera el máximo de existencias permitido.");
        }
        producto.addExistencias(request.cantidad());
        var saved = productoRepository.save(producto);
        var categoria = categoriaRepository.findById(saved.toDomain().categoriaId())
                .orElseThrow(() -> new IllegalStateException("No se encontró la categoría del producto."));
        return ResponseEntity.status(HttpStatus.CREATED).body(new IngresoProduccionResponse(
                saved.getId(),
                saved.getNombre(),
                categoria.getNombre(),
                saved.getCosto(),
                request.cantidad(),
                saved.getExistencias()
        ));
    }

    @GetMapping("/productos/{productoId}/variantes")
    public List<VariantDimension> listarVariantesProducto(@PathVariable int productoId) {
        validateProductExists(productoId);
        return getVariantDimensions(productoId);
    }

    @DeleteMapping("/productos/{productoId}/existencias")
    @Transactional
    public ExistenciasProductoResponse eliminarExistenciasProducto(@PathVariable int productoId) {
        if (productoId <= 0) {
            throw new IllegalArgumentException("Selecciona un producto válido.");
        }
        ProductoEntity producto = productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto seleccionado no existe."));
        producto.clearExistencias();
        productoRepository.save(producto);
        return new ExistenciasProductoResponse(producto.getId(), producto.getExistencias());
    }

    @GetMapping("/documentos/opciones")
    public List<OpcionCatalogo> listarDocumentos() {
        return documentoRepository.findAllByOrderByTipoAsc().stream()
                .map(documento -> new OpcionCatalogo(documento.getId(), documento.getTipo()))
                .toList();
    }

    @GetMapping("/ventas")
    public List<VentaResponse> listarVentas() {
        return pedidoRepository.findAll().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @PostMapping("/ventas")
    public ResponseEntity<VentaResponse> crearVenta(@RequestBody VentaRequest request) {
        if (request.usuarioId() != null
                && (request.usuarioId() <= 0 || !usuarioRepository.existsById(request.usuarioId()))) {
            throw new IllegalArgumentException("El usuario seleccionado no existe.");
        }
        PedidoEntity saved = pedidoRepository.save(new PedidoEntity(request.usuarioId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/ventas/productos")
    public List<ProductoVentaOption> listarProductosParaVenta() {
        return productoRepository.findAll().stream()
                .map(producto -> new ProductoVentaOption(
                        producto.getId(), producto.getNombre(), producto.getCosto(), producto.getExistencias()
                ))
                .toList();
    }

    @GetMapping("/reportes/rentabilidad")
    public RentabilidadResponse obtenerRentabilidad() {
        Map<Integer, ProductoEntity> productos = productoRepository.findAll().stream()
                .collect(Collectors.toMap(ProductoEntity::getId, producto -> producto));
        Map<Integer, RentabilidadProducto> productosVendidos = new HashMap<>();
        Set<Integer> ventasConProductos = new HashSet<>();
        BigDecimal ingresosBrutos = BigDecimal.ZERO;
        long unidadesVendidas = 0;

        for (DetallePedidoEntity detalle : detallePedidoRepository.findAll()) {
            ProductoEntity producto = productos.get(detalle.getProductoId());
            if (producto == null) {
                throw new IllegalStateException("No se encontró un producto asociado a una venta.");
            }
            BigDecimal subtotal = detalle.getCosto().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            RentabilidadProducto linea = new RentabilidadProducto(
                    producto.getId(), producto.getNombre(), detalle.getCantidad(),
                    subtotal, producto.getExistencias()
            );
            productosVendidos.merge(producto.getId(), linea, (actual, nueva) -> new RentabilidadProducto(
                    actual.productoId(),
                    actual.nombre(),
                    actual.unidadesVendidas() + nueva.unidadesVendidas(),
                    actual.ingresosBrutos().add(nueva.ingresosBrutos()),
                    actual.existenciasActuales()
            ));
            ventasConProductos.add(detalle.getPedidoId());
            unidadesVendidas += detalle.getCantidad();
            ingresosBrutos = ingresosBrutos.add(subtotal);
        }

        List<RentabilidadProducto> resumenProductos = productosVendidos.values().stream()
                .sorted(Comparator.comparing(RentabilidadProducto::ingresosBrutos).reversed()
                        .thenComparing(RentabilidadProducto::nombre))
                .toList();
        return new RentabilidadResponse(
                ventasConProductos.size(), unidadesVendidas, ingresosBrutos, resumenProductos
        );
    }

    @GetMapping("/ventas/{pedidoId}/productos")
    public VentaDetalleResponse listarProductosDeVenta(@PathVariable int pedidoId) {
        PedidoEntity pedido = findPedido(pedidoId);
        List<ItemVentaResponse> items = detallePedidoRepository
                .findAllByPedidoIdOrderByProductoIdAsc(pedido.getId()).stream()
                .map(this::toResponse)
                .toList();
        BigDecimal total = items.stream()
                .map(ItemVentaResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VentaDetalleResponse(pedido.getId(), items, total);
    }

    @PostMapping("/ventas/{pedidoId}/registrar")
    @Transactional
    public VentaResponse registrarVenta(@PathVariable int pedidoId) {
        PedidoEntity pedido = findPedidoForUpdate(pedidoId);
        ensurePending(pedido);
        List<DetallePedidoEntity> detalles = detallePedidoRepository
                .findAllByPedidoIdOrderByProductoIdAsc(pedido.getId());
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("No se puede registrar una venta sin productos.");
        }
        pedido.markAsPaid();
        return toResponse(pedidoRepository.save(pedido));
    }

    @PostMapping("/ventas/{pedidoId}/productos")
    @Transactional
    public ResponseEntity<VentaDetalleResponse> agregarProductoAVenta(
            @PathVariable int pedidoId,
            @RequestBody AgregarProductoVentaRequest request
    ) {
        PedidoEntity pedido = findPedidoForUpdate(pedidoId);
        if (request.productoId() <= 0) {
            throw new IllegalArgumentException("Selecciona un producto válido.");
        }
        if (request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un entero mayor que cero.");
        }
        ensurePending(pedido);
        ProductoEntity producto = productoRepository.findByIdForUpdate(request.productoId())
                .orElseThrow(() -> new IllegalArgumentException("El producto seleccionado no existe."));
        DetallePedidoId detalleId = new DetallePedidoId(pedido.getId(), producto.getId());
        DetallePedidoEntity detalle = detallePedidoRepository.findById(detalleId).orElse(null);
        int cantidadAnterior = detalle == null ? 0 : detalle.getCantidad();
        try {
            if (detalle == null) {
                detalle = new DetallePedidoEntity(
                        pedido.getId(), producto.getId(), request.cantidad(), producto.getCosto()
                );
            } else {
                detalle.addCantidad(request.cantidad());
            }
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("La cantidad acumulada supera el límite permitido.");
        }
        int incremento = detalle.getCantidad() - cantidadAnterior;
        if (incremento > producto.getExistencias()) {
            throw new IllegalArgumentException("La cantidad supera las existencias disponibles.");
        }
        if (cantidadAnterior > 0) {
            producto.subtractExistencias(incremento);
            productoRepository.save(producto);
        }
        detallePedidoRepository.save(detalle);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listarProductosDeVenta(pedido.getId()));
    }

    @PutMapping("/ventas/{pedidoId}/productos/{productoId}")
    @Transactional
    public VentaDetalleResponse actualizarCantidadProductoVenta(
            @PathVariable int pedidoId,
            @PathVariable int productoId,
            @RequestBody ActualizarCantidadRequest request
    ) {
        PedidoEntity pedido = findPedidoForUpdate(pedidoId);
        ensurePending(pedido);
        if (request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un entero mayor que cero.");
        }
        ProductoEntity producto = productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto seleccionado no existe."));
        DetallePedidoEntity detalle = detallePedidoRepository.findById(
                        new DetallePedidoId(pedido.getId(), producto.getId()))
                .orElseThrow(() -> new IllegalArgumentException("El producto no está en esta comanda."));
        int diferencia = request.cantidad() - detalle.getCantidad();
        if (diferencia > producto.getExistencias()) {
            throw new IllegalArgumentException("La cantidad supera las existencias disponibles.");
        }
        if (diferencia > 0) {
            producto.subtractExistencias(diferencia);
        } else if (diferencia < 0) {
            producto.addExistencias(-diferencia);
        }
        productoRepository.save(producto);
        detalle.setCantidad(request.cantidad());
        detallePedidoRepository.save(detalle);
        return listarProductosDeVenta(pedido.getId());
    }

    @DeleteMapping("/ventas/{pedidoId}/productos/{productoId}")
    @Transactional
    public VentaDetalleResponse quitarProductoDeVenta(
            @PathVariable int pedidoId,
            @PathVariable int productoId
    ) {
        PedidoEntity pedido = findPedidoForUpdate(pedidoId);
        ensurePending(pedido);
        DetallePedidoId detalleId = new DetallePedidoId(pedido.getId(), productoId);
        DetallePedidoEntity detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no está en esta comanda."));
        ProductoEntity producto = productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto seleccionado no existe."));
        producto.addExistencias(detalle.getCantidad());
        productoRepository.save(producto);
        detallePedidoRepository.delete(detalle);
        return listarProductosDeVenta(pedido.getId());
    }

    @GetMapping("/productos/{productoId}/colores")
    public List<ColorResponse> listarColores(@PathVariable int productoId) {
        validateProductExists(productoId);
        return colorRepository.findAllByProductoIdOrderByNombreAsc(productoId).stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @GetMapping("/colores")
    public List<ColorResponse> listarTodosLosColores() {
        return colorRepository.findAllByOrderByNombreAsc().stream()
                .map(AdministracionController::toResponse)
                .toList();
    }

    @PostMapping("/colores")
    public ResponseEntity<ColorResponse> crearColor(@RequestBody ColorRequest request) {
        validateProductExists(request.productoId());
        String nombre = required(request.nombre(), "El nombre del color es obligatorio.");
        validateLength(nombre, 50, "El nombre del color admite máximo 50 caracteres.");
        String codigoHex = request.codigoHex() == null || request.codigoHex().isBlank()
                ? "#14B8A6" : request.codigoHex().trim().toUpperCase();
        if (!codigoHex.matches("^#[0-9A-F]{6}$")) {
            throw new IllegalArgumentException("El código del color debe tener formato hexadecimal, por ejemplo #14B8A6.");
        }
        if (colorRepository.existsByProductoIdAndNombreIgnoreCase(request.productoId(), nombre)) {
            throw new IllegalArgumentException("Ese color ya está registrado para el producto.");
        }
        ColorEntity saved = colorRepository.save(new ColorEntity(request.productoId(), nombre, codigoHex));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/tallas")
    public List<TallaResponse> listarTallas() {
        Map<Integer, Long> asignacionesPorTalla = productoTallaRepository.findAll().stream()
                .collect(Collectors.groupingBy(ProductoTallaEntity::getTallaId, Collectors.counting()));
        return tallaRepository.findAllByOrderBySistemaAscOrdenVisualAscNombreDescriptivoAsc().stream()
                .map(talla -> toResponse(talla, asignacionesPorTalla.getOrDefault(talla.getId(), 0L).intValue()))
                .toList();
    }

    @PostMapping("/tallas")
    public ResponseEntity<TallaResponse> crearTalla(@RequestBody TallaRequest request) {
        String codigo = required(request.codigo(), "El código corto de la talla es obligatorio.").toUpperCase();
        String nombre = required(request.nombre(), "El nombre descriptivo de la talla es obligatorio.");
        String sistema = required(request.sistema(), "Selecciona un sistema de tallaje.");
        String estado = request.estado() == null || request.estado().isBlank()
                ? "Activo" : request.estado().trim();
        validateLength(codigo, 20, "El código admite máximo 20 caracteres.");
        validateLength(nombre, 100, "El nombre descriptivo admite máximo 100 caracteres.");
        validateLength(sistema, 30, "El sistema admite máximo 30 caracteres.");
        if (!sistema.equalsIgnoreCase("Adulto") && !sistema.equalsIgnoreCase("Infantil")
                && !sistema.equalsIgnoreCase("Sin definir")) {
            throw new IllegalArgumentException("El sistema debe ser Adulto, Infantil o Sin definir.");
        }
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        if (request.ordenVisual() < 0) {
            throw new IllegalArgumentException("El orden visual debe ser cero o mayor.");
        }
        String sistemaNormalizado = sistema.equalsIgnoreCase("adulto") ? "Adulto"
                : sistema.equalsIgnoreCase("infantil") ? "Infantil" : "Sin definir";
        String estadoNormalizado = estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo";
        if (tallaRepository.existsByCodigoIgnoreCaseAndSistemaIgnoreCase(codigo, sistemaNormalizado)) {
            throw new IllegalArgumentException("Ya existe ese código en el sistema de tallaje seleccionado.");
        }
        TallaEntity saved = tallaRepository.save(new TallaEntity(
                codigo, nombre, sistemaNormalizado, request.ordenVisual(), estadoNormalizado
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved, 0));
    }

    @PutMapping("/tallas/{tallaId}/estado")
    public TallaResponse actualizarEstadoTalla(
            @PathVariable int tallaId,
            @RequestBody TallaEstadoRequest request
    ) {
        String estado = request.estado() == null ? "" : request.estado().trim();
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        TallaEntity talla = tallaRepository.findById(tallaId)
                .orElseThrow(() -> new IllegalArgumentException("La talla seleccionada no existe."));
        talla.setEstado(estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo");
        TallaEntity saved = tallaRepository.save(talla);
        return toResponse(saved, productoTallaRepository.findAllByTallaId(tallaId).size());
    }

    @GetMapping("/productos/{productoId}/tallas")
    public List<TallaResponse> listarTallasDeProducto(@PathVariable int productoId) {
        validateProductExists(productoId);
        return productoTallaRepository.findAllByProductoId(productoId).stream()
                .map(ProductoTallaEntity::getTallaId)
                .map(tallaId -> tallaRepository.findById(tallaId)
                        .orElseThrow(() -> new IllegalStateException("No se encontró una talla asignada.")))
                .map(talla -> toResponse(talla, productoTallaRepository.findAllByTallaId(talla.getId()).size()))
                .toList();
    }

    @PostMapping("/productos/{productoId}/tallas/{tallaId}")
    public TallaResponse asignarTallaAProducto(@PathVariable int productoId, @PathVariable int tallaId) {
        validateProductExists(productoId);
        TallaEntity talla = tallaRepository.findById(tallaId)
                .orElseThrow(() -> new IllegalArgumentException("La talla seleccionada no existe."));
        if (!"Activo".equalsIgnoreCase(talla.getEstado())) {
            throw new IllegalArgumentException("No se puede asignar una talla inactiva.");
        }
        if (productoTallaRepository.existsByProductoIdAndTallaId(productoId, tallaId)) {
            throw new IllegalArgumentException("La talla ya está asignada a este producto.");
        }
        productoTallaRepository.save(new ProductoTallaEntity(productoId, tallaId));
        return toResponse(talla, productoTallaRepository.findAllByTallaId(tallaId).size());
    }

    @DeleteMapping("/productos/{productoId}/tallas/{tallaId}")
    public ResponseEntity<Void> quitarTallaDeProducto(@PathVariable int productoId, @PathVariable int tallaId) {
        validateProductExists(productoId);
        if (!productoTallaRepository.existsByProductoIdAndTallaId(productoId, tallaId)) {
            throw new IllegalArgumentException("La talla no está asignada a este producto.");
        }
        productoTallaRepository.deleteByProductoIdAndTallaId(productoId, tallaId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProductoController.ErrorResponse> manejarDatosInvalidos(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(new ProductoController.ErrorResponse(exception.getMessage()));
    }

    private void validateProductExists(int productoId) {
        if (productoId <= 0 || !productoRepository.existsById(productoId)) {
            throw new IllegalArgumentException("El producto seleccionado no existe.");
        }
    }

    private List<VariantDimension> getVariantDimensions(int productoId) {
        List<ColorEntity> colores = colorRepository.findAllByProductoIdOrderByNombreAsc(productoId);
        List<TallaEntity> tallas = productoTallaRepository.findAllByProductoId(productoId).stream()
                .map(ProductoTallaEntity::getTallaId)
                .map(tallaId -> tallaRepository.findById(tallaId)
                        .orElseThrow(() -> new IllegalStateException("No se encontró una talla asignada.")))
                .sorted(Comparator.comparing(TallaEntity::getSistema)
                        .thenComparingInt(TallaEntity::getOrdenVisual)
                        .thenComparing(TallaEntity::getNombreDescriptivo))
                .toList();

        List<VariantDimension> dimensions = new java.util.ArrayList<>();
        if (colores.isEmpty() && tallas.isEmpty()) {
            dimensions.add(new VariantDimension(null, null, null, null));
        } else if (colores.isEmpty()) {
            tallas.forEach(talla -> dimensions.add(new VariantDimension(
                    null, null, talla.getId(), talla.getNombreDescriptivo()
            )));
        } else if (tallas.isEmpty()) {
            colores.forEach(color -> dimensions.add(new VariantDimension(
                    color.getId(), color.getNombre(), null, null
            )));
        } else {
            colores.forEach(color -> tallas.forEach(talla -> dimensions.add(new VariantDimension(
                    color.getId(), color.getNombre(), talla.getId(), talla.getNombreDescriptivo()
            ))));
        }
        return dimensions;
    }

    private PedidoEntity findPedido(int pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("La venta seleccionada no existe."));
    }

    private PedidoEntity findPedidoForUpdate(int pedidoId) {
        return pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("La venta seleccionada no existe."));
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void validateLength(String value, int maxLength, String message) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNonNegativeDecimal(BigDecimal value, String field) {
        if (value == null || value.signum() < 0 || value.scale() > 2 || value.precision() > 10) {
            throw new IllegalArgumentException(
                    field + " debe ser no negativo, tener máximo dos decimales y caber en DECIMAL(10,2)."
            );
        }
    }

    private static CategoriaResponse toResponse(CategoriaEntity categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getSlug(),
                categoria.getDescripcion(), categoria.getImagenUrl(), categoria.getEstado());
    }

    private static EmpleadoResponse toResponse(EmpleadoEntity empleado) {
        return new EmpleadoResponse(empleado.getId(), empleado.getDocumentoId(), empleado.getNombre(),
                empleado.getRolId(), empleado.getRol(), empleado.getEstado(),
                empleado.getTelefono(), empleado.getCorreo());
    }

    private static UsuarioResponse toResponse(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getDocumentoId(), usuario.getCiudadId(), usuario.getNombre(),
                usuario.getDireccion(), usuario.getTelefono(), usuario.getCorreo(),
                usuario.getFechaNacimiento(), usuario.getFechaRegistro()
        );
    }

    private static InsumoMenorResponse toResponse(InsumoMenorEntity insumo) {
        return new InsumoMenorResponse(
                insumo.getId(), insumo.getNombre(), insumo.getReferencia(), insumo.getUnidadMedida(),
                insumo.getCostoUnitario(), insumo.getCantidadDisponible()
        );
    }

    private static VentaResponse toResponse(PedidoEntity pedido) {
        return new VentaResponse(pedido.getId(), pedido.getUsuarioId(), pedido.getFechaCreacion(), pedido.getEstado());
    }

    private static ColorResponse toResponse(ColorEntity color) {
        return new ColorResponse(color.getId(), color.getProductoId(), color.getNombre(), color.getCodigoHex());
    }

    private static TallaResponse toResponse(TallaEntity talla, int productos) {
        return new TallaResponse(talla.getId(), talla.getCodigo(), talla.getNombreDescriptivo(),
                talla.getSistema(), talla.getOrdenVisual(), talla.getEstado(), productos);
    }

    private ItemVentaResponse toResponse(DetallePedidoEntity detalle) {
        var producto = productoRepository.findById(detalle.getProductoId())
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el producto asociado al detalle de la venta."
                ));
        return new ItemVentaResponse(
                detalle.getProductoId(),
                producto.getNombre(),
                detalle.getCantidad(),
                detalle.getCosto(),
                detalle.getCosto().multiply(BigDecimal.valueOf(detalle.getCantidad()))
        );
    }

    public record CategoriaRequest(String nombre, String slug, String descripcion, String imagenUrl, String estado) {}
    public record CategoriaEstadoRequest(String estado) {}
    public record CategoriaResponse(
            Integer id, String nombre, String slug, String descripcion, String imagenUrl, String estado
    ) {}
    public record EmpleadoRequest(
            int id, int documentoId, String nombre, int rolId, String estado, String telefono,
            String correo, String contrasena
    ) {}
    public record ActualizarRolEmpleadoRequest(int rolId) {}
    public record ActualizarContrasenaEmpleadoRequest(String contrasena) {}
    public record EmpleadoResponse(
            Integer id, Integer documentoId, String nombre, Integer rolId, String rol, String estado,
            String telefono, String correo
    ) {}
    public record VentaRequest(Integer usuarioId) {}
    public record VentaResponse(
            Integer id, Integer usuarioId, java.time.LocalDateTime fechaCreacion, String estado
    ) {}
    public record ProductoVentaOption(Integer id, String nombre, BigDecimal costo, int existencias) {}
    public record VariantDimension(Integer colorId, String color, Integer tallaId, String talla) {}
    public record RentabilidadResponse(
            int ventasConProductos, long unidadesVendidas, BigDecimal ingresosBrutos,
            List<RentabilidadProducto> productos
    ) {}
    public record RentabilidadProducto(
            Integer productoId, String nombre, long unidadesVendidas,
            BigDecimal ingresosBrutos, int existenciasActuales
    ) {}
    public record AgregarProductoVentaRequest(int productoId, int cantidad) {}
    public record ActualizarCantidadRequest(int cantidad) {}
    public record ItemVentaResponse(
            Integer productoId, String nombre, int cantidad, BigDecimal costo, BigDecimal subtotal
    ) {}
    public record VentaDetalleResponse(Integer pedidoId, List<ItemVentaResponse> items, BigDecimal total) {}
    public record VarianteRequest(int productoId, String nombre) {}
    public record ColorRequest(int productoId, String nombre, String codigoHex) {}
    public record ColorResponse(Integer id, Integer productoId, String nombre, String codigoHex) {}
    public record TallaRequest(
            String codigo, String nombre, String sistema, int ordenVisual, String estado
    ) {}
    public record TallaEstadoRequest(String estado) {}
    public record TallaResponse(
            Integer id, String codigo, String nombre, String sistema, int ordenVisual, String estado, int productos
    ) {}
    public record UsuarioRequest(
            long id, int documentoId, int ciudadId, String nombre, String direccion, String telefono,
            String correo, LocalDate fechaNacimiento, String contrasena, String confirmarContrasena
    ) {}
    public record UsuarioResponse(
            Integer id, Integer documentoId, Integer ciudadId, String nombre, String direccion,
            String telefono, String correo, LocalDate fechaNacimiento, java.time.LocalDateTime fechaRegistro
    ) {}
    public record UsuarioCatalogOptions(List<OpcionCatalogo> documentos, List<OpcionCatalogo> ciudades) {}
    public record IngresoProduccionRequest(int productoId, int cantidad) {}
    public record IngresoProduccionResponse(
            Integer productoId, String nombre, String categoria, BigDecimal costo, int cantidadIngresada,
            int existenciasActuales
    ) {}
    public record ExistenciasProductoResponse(Integer productoId, int existencias) {}
    public record InsumoMenorRequest(
            String nombre, String referencia, String unidadMedida,
            BigDecimal costoUnitario, BigDecimal cantidadDisponible
    ) {}
    public record InsumoMenorResponse(
            Integer id, String nombre, String referencia, String unidadMedida,
            BigDecimal costoUnitario, BigDecimal cantidadDisponible
    ) {}
}

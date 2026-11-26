const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('inventario', {
  iniciarSesion: (credenciales) => ipcRenderer.invoke('auth:login', credenciales),
  cerrarSesion: () => ipcRenderer.invoke('auth:logout'),
  cambiarMiContrasena: (cambio) => ipcRenderer.invoke('auth:change-password', cambio),
  listarProductos: () => ipcRenderer.invoke('productos:listar'),
  obtenerRentabilidad: () => ipcRenderer.invoke('reportes:rentabilidad'),
  obtenerOpciones: () => ipcRenderer.invoke('productos:opciones'),
  obtenerVariantesProducto: (productoId) => ipcRenderer.invoke('productos:variantes', productoId),
  crearProducto: (producto) => ipcRenderer.invoke('productos:crear', producto),
  obtenerRegistros: (recurso) => ipcRenderer.invoke('administracion:obtener', recurso),
  crearRegistro: (recurso, registro) => ipcRenderer.invoke('administracion:crear', recurso, registro),
  eliminarExistenciasProducto: (productoId) =>
    ipcRenderer.invoke('productos:eliminar-existencias', productoId),
  actualizarEstadoCategoria: (categoriaId, estado) =>
    ipcRenderer.invoke('categorias:actualizar-estado', categoriaId, estado),
  actualizarRolEmpleado: (empleadoId, rolId) =>
    ipcRenderer.invoke('empleados:actualizar-rol', empleadoId, rolId),
  retirarEmpleado: (empleadoId) => ipcRenderer.invoke('empleados:retirar', empleadoId),
  actualizarContrasenaEmpleado: (empleadoId, contrasena) =>
    ipcRenderer.invoke('empleados:actualizar-contrasena', empleadoId, contrasena),
  asignarTallaAProducto: (productoId, tallaId) =>
    ipcRenderer.invoke('tallas:asignar-producto', productoId, tallaId),
  listarVariantes: (recurso, productoId) =>
    ipcRenderer.invoke('administracion:variantes', recurso, productoId),
  obtenerDetalleVenta: (pedidoId) => ipcRenderer.invoke('ventas:detalle', pedidoId),
  agregarProductoAVenta: (pedidoId, detalle) =>
    ipcRenderer.invoke('ventas:agregar-producto', pedidoId, detalle),
  actualizarCantidadVenta: (pedidoId, productoId, cantidad) =>
    ipcRenderer.invoke('ventas:actualizar-cantidad', pedidoId, productoId, cantidad),
  quitarProductoDeVenta: (pedidoId, productoId) =>
    ipcRenderer.invoke('ventas:quitar-producto', pedidoId, productoId),
  registrarVenta: (pedidoId) => ipcRenderer.invoke('ventas:registrar', pedidoId),
});

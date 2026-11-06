const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('inventario', {
  listarProductos: () => ipcRenderer.invoke('productos:listar'),
  obtenerOpciones: () => ipcRenderer.invoke('productos:opciones'),
  crearProducto: (producto) => ipcRenderer.invoke('productos:crear', producto),
  obtenerRegistros: (recurso) => ipcRenderer.invoke('administracion:obtener', recurso),
  crearRegistro: (recurso, registro) => ipcRenderer.invoke('administracion:crear', recurso, registro),
  actualizarEstadoCategoria: (categoriaId, estado) =>
    ipcRenderer.invoke('categorias:actualizar-estado', categoriaId, estado),
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
});

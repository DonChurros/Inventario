const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('node:path');

const apiBaseUrl = (process.env.INVENTARIO_API_URL || 'http://localhost:8080').replace(/\/+$/, '');

async function requestApi(path, options) {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    signal: AbortSignal.timeout(10000),
  });
  const responseBody = await response.text();

  if (!response.ok) {
    let message = responseBody || `El servidor respondió con estado ${response.status}.`;
    try {
      const error = JSON.parse(responseBody);
      message = error.error || error.message || message;
    } catch {
      // Conserva el cuerpo original cuando Spring devuelve un error que no es JSON.
    }
    throw new Error(message);
  }

  return responseBody ? JSON.parse(responseBody) : null;
}

function createWindow() {
  const window = new BrowserWindow({
    width: 1180,
    height: 800,
    minWidth: 760,
    minHeight: 640,
    title: 'Erik’s Creaciones | Administración',
    backgroundColor: '#f7f6f6',
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  if (process.env.VITE_DEV_SERVER_URL) {
    window.loadURL(process.env.VITE_DEV_SERVER_URL);
  } else {
    window.loadFile(path.join(__dirname, '..', 'dist', 'index.html'));
  }
}

ipcMain.handle('productos:listar', () => requestApi('/productos'));

ipcMain.handle('productos:opciones', () => requestApi('/productos/opciones'));

ipcMain.handle('productos:crear', (_event, producto) => requestApi('/productos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(producto),
  }));

ipcMain.handle('administracion:obtener', (_event, recurso) => {
  const paths = {
    categorias: '/categorias',
    empleados: '/empleados',
    roles: '/roles/opciones',
    usuarios: '/usuarios/opciones',
    usuariosCompletos: '/usuarios',
    catalogosUsuario: '/usuarios/catalogos',
    insumos: '/insumos',
    ventas: '/ventas',
    productosVenta: '/ventas/productos',
    documentos: '/documentos/opciones',
    productos: '/productos',
    colores: '/colores',
    tallas: '/tallas',
  };
  const path = paths[recurso];
  if (!path) {
    throw new Error('El recurso solicitado no está disponible.');
  }
  return requestApi(path);
});

ipcMain.handle('administracion:crear', (_event, recurso, registro) => {
  const paths = {
    categorias: '/categorias',
    empleados: '/empleados',
    usuarios: '/usuarios',
    insumos: '/insumos',
    ventas: '/ventas',
    colores: '/colores',
    tallas: '/tallas',
    produccion: '/produccion/ingresos',
  };
  const path = paths[recurso];
  if (!path) {
    throw new Error('No se permite crear este tipo de registro.');
  }
  return requestApi(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(registro),
  });
});

ipcMain.handle('categorias:actualizar-estado', (_event, categoriaId, estado) => {
  const id = Number(categoriaId);
  if (!Number.isInteger(id) || id <= 0 || !['Activo', 'Inactivo'].includes(estado)) {
    throw new Error('Selecciona una categoría y un estado válidos.');
  }
  return requestApi(`/categorias/${id}/estado`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ estado }),
  });
});

ipcMain.handle('tallas:asignar-producto', (_event, productoId, tallaId) => {
  const productId = Number(productoId);
  const sizeId = Number(tallaId);
  if (!Number.isInteger(productId) || productId <= 0 || !Number.isInteger(sizeId) || sizeId <= 0) {
    throw new Error('Selecciona un producto y una talla válidos.');
  }
  return requestApi(`/productos/${productId}/tallas/${sizeId}`, { method: 'POST' });
});

ipcMain.handle('administracion:variantes', (_event, recurso, productoId) => {
  const paths = {
    colores: `/productos/${Number(productoId)}/colores`,
    tallas: `/productos/${Number(productoId)}/tallas`,
  };
  const path = paths[recurso];
  if (!path || !Number.isInteger(Number(productoId)) || Number(productoId) <= 0) {
    throw new Error('Selecciona un producto válido.');
  }
  return requestApi(path);
});

ipcMain.handle('ventas:detalle', (_event, pedidoId) => {
  const id = Number(pedidoId);
  if (!Number.isInteger(id) || id <= 0) {
    throw new Error('Selecciona una venta válida.');
  }
  return requestApi(`/ventas/${id}/productos`);
});

ipcMain.handle('ventas:agregar-producto', (_event, pedidoId, detalle) => {
  const id = Number(pedidoId);
  if (!Number.isInteger(id) || id <= 0) {
    throw new Error('Selecciona una venta válida.');
  }
  return requestApi(`/ventas/${id}/productos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(detalle),
  });
});

ipcMain.handle('ventas:actualizar-cantidad', (_event, pedidoId, productoId, cantidad) => {
  const orderId = Number(pedidoId);
  const itemId = Number(productoId);
  if (!Number.isInteger(orderId) || orderId <= 0 || !Number.isInteger(itemId) || itemId <= 0) {
    throw new Error('Selecciona una venta y un producto válidos.');
  }
  return requestApi(`/ventas/${orderId}/productos/${itemId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ cantidad: Number(cantidad) }),
  });
});

ipcMain.handle('ventas:quitar-producto', (_event, pedidoId, productoId) => {
  const orderId = Number(pedidoId);
  const itemId = Number(productoId);
  if (!Number.isInteger(orderId) || orderId <= 0 || !Number.isInteger(itemId) || itemId <= 0) {
    throw new Error('Selecciona una venta y un producto válidos.');
  }
  return requestApi(`/ventas/${orderId}/productos/${itemId}`, { method: 'DELETE' });
});

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

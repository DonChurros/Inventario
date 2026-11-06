import { useEffect, useState } from 'react';
import ManagementPanel from './ManagementPanel.jsx';
import ProductionPanel from './ProductionPanel.jsx';
import EmployeesPanel from './EmployeesPanel.jsx';
import CustomersPanel from './CustomersPanel.jsx';
import InsumosPanel from './InsumosPanel.jsx';
import CategoriesPanel from './CategoriesPanel.jsx';
import ColorsPanel from './ColorsPanel.jsx';
import SizesPanel from './SizesPanel.jsx';

const primaryLinks = [
  ['inicio', 'Inicio', '⌂'],
  ['ventas', 'Ventas', '▧'],
  ['produccion', 'Producción', '◇'],
  ['productos', 'Productos', '☷'],
  ['clientes', 'Clientes', '♧'],
  ['reportes', 'Reportes', '▤'],
];

const settingsLinks = [
  ['empleados', 'Empleados'],
  ['categorias', 'Categorías'],
  ['colores', 'Colores'],
  ['tallas', 'Tallas'],
  ['usuarios', 'Usuarios'],
  ['insumos', 'Insumos menores'],
];

const initialForm = {
  categoriaId: '',
  empleadoId: '',
  nombre: '',
  slug: '',
  descripcion: '',
  costo: '',
  imagenUrl: '',
  existencias: '0',
  destacado: false,
};

function App() {
  const [form, setForm] = useState(initialForm);
  const [options, setOptions] = useState({ categorias: [], empleados: [] });
  const [products, setProducts] = useState([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [productsError, setProductsError] = useState('');
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState(null);
  const [activeView, setActiveView] = useState('inicio');
  const [settingsView, setSettingsView] = useState('empleados');

  useEffect(() => {
    if (!window.inventario) {
      setLoadingOptions(false);
      setLoadingProducts(false);
      setProductsError('La conexión con la base está disponible al iniciar la app de escritorio.');
      return undefined;
    }
    let active = true;
    Promise.allSettled([
      window.inventario.obtenerOpciones(),
      window.inventario.listarProductos(),
    ])
      .then(([catalogResult, productsResult]) => {
        if (!active) return;
        if (catalogResult.status === 'fulfilled') {
          setOptions(catalogResult.value);
        } else {
          setNotice({ type: 'error', text: catalogResult.reason.message });
        }
        if (productsResult.status === 'fulfilled') {
          setProducts(productsResult.value);
          setProductsError('');
        } else {
          setProductsError(productsResult.reason.message || 'No se pudieron cargar los productos.');
        }
      })
      .finally(() => {
        if (active) {
          setLoadingOptions(false);
          setLoadingProducts(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  async function refreshProducts() {
    setLoadingProducts(true);
    setProductsError('');
    try {
      setProducts(await window.inventario.listarProductos());
    } catch (error) {
      setProductsError(error.message || 'No se pudieron cargar los productos.');
    } finally {
      setLoadingProducts(false);
    }
  }

  function updateField(event) {
    const { name, value, type, checked } = event.target;
    setForm((current) => {
      const next = { ...current, [name]: type === 'checkbox' ? checked : value };
      if (name === 'nombre' && (!current.slug || current.slug === slugify(current.nombre))) {
        next.slug = slugify(value);
      }
      return next;
    });
    setNotice(null);
  }

  async function submitProduct(event) {
    event.preventDefault();
    setNotice(null);

    const product = {
      categoriaId: Number(form.categoriaId),
      empleadoId: Number(form.empleadoId),
      nombre: form.nombre.trim(),
      slug: form.slug.trim(),
      descripcion: form.descripcion.trim() || null,
      costo: Number(form.costo),
      imagenUrl: form.imagenUrl.trim(),
      existencias: Number(form.existencias),
      destacado: form.destacado,
    };

    if (!product.categoriaId || !product.empleadoId) {
      setNotice({ type: 'error', text: 'Selecciona una categoría y el empleado responsable.' });
      return;
    }
    if (!product.nombre || product.nombre.length > 100 || !product.slug || product.slug.length > 100) {
      setNotice({ type: 'error', text: 'El nombre y el slug son obligatorios (máximo 100 caracteres).' });
      return;
    }
    if (!Number.isFinite(product.costo) || product.costo < 0 || !/^\d+(\.\d{1,2})?$/.test(form.costo)) {
      setNotice({ type: 'error', text: 'Ingresa un costo válido, no negativo y con máximo dos decimales.' });
      return;
    }
    if (!product.imagenUrl || product.imagenUrl.length > 255) {
      setNotice({ type: 'error', text: 'La URL de imagen es obligatoria y debe tener máximo 255 caracteres.' });
      return;
    }
    if (!Number.isInteger(product.existencias) || product.existencias < 0) {
      setNotice({ type: 'error', text: 'Las existencias deben ser un entero igual o mayor a cero.' });
      return;
    }

    setSaving(true);
    try {
      const created = await window.inventario.crearProducto(product);
      setProducts((current) => [created, ...current]);
      setNotice({
        type: 'success',
        text: `Producto guardado en la base de datos · ID ${created.id}.`,
      });
      setForm(initialForm);
    } catch (error) {
      const detail = error?.message?.includes('fetch failed')
        || error?.message?.toLowerCase().includes('timeout')
        ? 'No se pudo conectar con el backend. Verifica que Spring Boot esté iniciado.'
        : error?.message || 'Ocurrió un error inesperado.';
      setNotice({ type: 'error', text: detail });
    } finally {
      setSaving(false);
    }
  }

  const optionsReady = !loadingOptions
    && options.categorias.length > 0
    && options.empleados.length > 0;

  return (
    <main className="app-shell">
      <aside className="app-sidebar">
        <a className="brand" href="#" onClick={(event) => { event.preventDefault(); setActiveView('inicio'); }} aria-label="Erik's Creaciones, inicio">
          <span className="brand-mark" aria-hidden="true">♡</span>
          <span className="brand-name">Erik’s<small>Creaciones</small></span>
        </a>
        <nav className="sidebar-nav" aria-label="Navegación principal">
          <span className="sidebar-group-label">PRINCIPAL</span>
          {primaryLinks.map(([view, label, icon]) => (
            <button
              key={view}
              className={`sidebar-link ${activeView === view ? 'active' : ''}`}
              type="button"
              onClick={() => setActiveView(view)}
              aria-current={activeView === view ? 'page' : undefined}
            >
              <span className="sidebar-icon" aria-hidden="true">{icon}</span>
              <span>{label}</span>
              {activeView === view && <i aria-hidden="true" />}
            </button>
          ))}
          <span className="sidebar-group-label management-label">GESTIÓN</span>
          <button className={`sidebar-link ${activeView === 'movimientos' ? 'active' : ''}`} type="button" onClick={() => setActiveView('movimientos')}>
            <span className="sidebar-icon" aria-hidden="true">◷</span><span>Movimientos</span>
          </button>
          <button className={`sidebar-link ${activeView === 'configuracion' ? 'active' : ''}`} type="button" onClick={() => setActiveView('configuracion')}>
            <span className="sidebar-icon" aria-hidden="true">⚙</span><span>Configuración</span>
          </button>
          {activeView === 'configuracion' && (
            <div className="sidebar-subnav" aria-label="Opciones de configuración">
              {settingsLinks.map(([view, label]) => (
                <button
                  key={view}
                  type="button"
                  className={settingsView === view ? 'sidebar-sublink active' : 'sidebar-sublink'}
                  onClick={() => setSettingsView(view)}
                >{label}</button>
              ))}
            </div>
          )}
        </nav>
        <div className="sidebar-profile">
          <span className="profile-avatar">EC</span>
          <span><strong>Erik’s Creaciones</strong><small>Administración</small></span>
          <span className="profile-chevron">›</span>
        </div>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div className="topbar-context">
            <span>ERIK’S CREACIONES</span>
            <strong>{activeView === 'ventas' ? 'Administración de ventas' : activeView === 'produccion' ? 'Ingreso de producción' : activeView === 'productos' ? 'Inventario' : activeView === 'clientes' ? 'Clientes y accesos' : activeView === 'configuracion' ? 'Configuración' : activeView === 'reportes' ? 'Reportes' : activeView === 'movimientos' ? 'Movimientos' : 'Panel de administración'}</strong>
          </div>
          <div className="topbar-status"><span className="status-dot" /><span>Gestión de inventario</span></div>
        </header>

      <div className="main-content">
      {activeView === 'inicio' && <HomePanel onNavigate={setActiveView} />}
      {activeView === 'ventas' && <ManagementPanel view="ventas" onNavigate={setActiveView} />}
      {activeView === 'clientes' && <CustomersPanel />}
      {activeView === 'configuracion' && settingsView === 'empleados' && <EmployeesPanel />}
      {activeView === 'configuracion' && settingsView === 'usuarios' && <CustomersPanel />}
      {activeView === 'configuracion' && settingsView === 'insumos' && <InsumosPanel />}
      {activeView === 'configuracion' && settingsView === 'categorias' && <CategoriesPanel />}
      {activeView === 'configuracion' && settingsView === 'colores' && <ColorsPanel />}
      {activeView === 'configuracion' && settingsView === 'tallas' && <SizesPanel />}
      {activeView === 'configuracion' && !['empleados', 'usuarios', 'insumos', 'categorias', 'colores', 'tallas'].includes(settingsView) && <ManagementPanel view={settingsView} />}
      {activeView === 'produccion' && <ProductionPanel />}
      {activeView === 'movimientos' && (
        <InformationPanel
          title="Movimientos"
          description="Las ventas pendientes aparecen en el módulo Ventas. La salida de inventario se mantiene sin cambios hasta confirmar la venta."
          action="Ir a ventas"
          onAction={() => setActiveView('ventas')}
        />
      )}
      {activeView === 'reportes' && (
        <InformationPanel
          title="Reportes"
          description="Los reportes estarán disponibles cuando se definan los indicadores y el esquema de cierre de ventas."
          action="Ver inventario"
          onAction={() => setActiveView('productos')}
        />
      )}
      {activeView === 'productos' && <>
      <section className="workspace">
        <aside className="side-note">
          <span className="eyebrow">CATÁLOGO</span>
          <h1>Productos</h1>
          <p>Registra productos con los datos y relaciones requeridos por el catálogo.</p>
          <div className="side-divider" />
          <div className="side-step">
            <span className="step-number">01</span>
            <span>Crear producto</span>
          </div>
          <div className="side-step muted">
            <span className="step-number">02</span>
            <span>Asigna categoría y empleado</span>
          </div>
        </aside>

        <section className="form-card" aria-labelledby="form-title">
          <div className="card-heading">
            <div className="heading-icon" aria-hidden="true">＋</div>
            <div>
              <span className="eyebrow">NUEVO REGISTRO</span>
              <h2 id="form-title">Crear producto</h2>
            </div>
          </div>
          <p className="form-intro">El ID y las fechas se generan automáticamente en la base de datos.</p>

          <form onSubmit={submitProduct} noValidate>
            <div className="field-row">
              <label className="field">
                <span>Categoría</span>
                <select
                  name="categoriaId"
                  value={form.categoriaId}
                  onChange={updateField}
                  required
                  disabled={loadingOptions || options.categorias.length === 0}
                >
                  <option value="">
                    {loadingOptions ? 'Cargando categorías…' : 'Selecciona una categoría'}
                  </option>
                  {options.categorias.map((categoria) => (
                    <option key={categoria.id} value={categoria.id}>{categoria.nombre}</option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Empleado responsable</span>
                <select
                  name="empleadoId"
                  value={form.empleadoId}
                  onChange={updateField}
                  required
                  disabled={loadingOptions || options.empleados.length === 0}
                >
                  <option value="">
                    {loadingOptions ? 'Cargando empleados…' : 'Selecciona un empleado'}
                  </option>
                  {options.empleados.map((empleado) => (
                    <option key={empleado.id} value={empleado.id}>{empleado.nombre} · #{empleado.id}</option>
                  ))}
                </select>
              </label>
            </div>

            <div className="field-row">
              <label className="field">
                <span>Nombre</span>
                <input
                  autoFocus
                  name="nombre"
                  type="text"
                  placeholder="Ej. Churro clásico"
                  maxLength="100"
                  value={form.nombre}
                  onChange={updateField}
                  required
                />
              </label>
              <label className="field">
                <span>Slug</span>
                <input
                  name="slug"
                  type="text"
                  placeholder="churro-clasico"
                  maxLength="100"
                  value={form.slug}
                  onChange={updateField}
                  required
                />
              </label>
            </div>

            <label className="field">
              <span>Descripción <span className="optional">· opcional</span></span>
              <textarea
                name="descripcion"
                rows="3"
                placeholder="Descripción del producto"
                value={form.descripcion}
                onChange={updateField}
              />
            </label>

            <label className="field">
              <span>URL de imagen</span>
              <input
                name="imagenUrl"
                type="text"
                placeholder="https://ejemplo.com/imagen.jpg"
                maxLength="255"
                value={form.imagenUrl}
                onChange={updateField}
                required
              />
            </label>

            <div className="field-row">
              <label className="field">
                <span>Costo</span>
                <div className="price-input">
                  <span>$</span>
                  <input
                    name="costo"
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="0.00"
                    value={form.costo}
                    onChange={updateField}
                    required
                  />
                  <span className="currency">MXN</span>
                </div>
              </label>
              <label className="field">
                <span>Existencias</span>
                <input
                  name="existencias"
                  type="number"
                  min="0"
                  step="1"
                  value={form.existencias}
                  onChange={updateField}
                  required
                />
              </label>
            </div>

            <label className="checkbox-field">
              <input
                name="destacado"
                type="checkbox"
                checked={form.destacado}
                onChange={updateField}
              />
              <span>Marcar como producto destacado</span>
            </label>

            {!loadingOptions && (!options.categorias.length || !options.empleados.length) && (
              <div className="notice error" role="status">
                No hay categorías o empleados registrados en la base de datos.
              </div>
            )}

            {notice && (
              <div className={`notice ${notice.type}`} role="status">
                <span aria-hidden="true">{notice.type === 'success' ? '✓' : '!'}</span>
                {notice.text}
              </div>
            )}

            <div className="form-footer">
              <span className="required-note"><span>*</span> Campos obligatorios</span>
              <button type="submit" disabled={saving || loadingOptions || !optionsReady}>
                {saving ? 'Guardando…' : 'Guardar producto'}
                {!saving && <span aria-hidden="true">→</span>}
              </button>
            </div>
          </form>
        </section>
      </section>

      <section className="products-section" aria-labelledby="products-title">
        <div className="products-heading">
          <div>
            <span className="eyebrow">BASE DE DATOS</span>
            <h2 id="products-title">Productos registrados</h2>
          </div>
          <button
            className="refresh-button"
            type="button"
            onClick={refreshProducts}
            disabled={loadingProducts}
          >
            {loadingProducts ? 'Cargando…' : 'Actualizar'}
          </button>
        </div>

        {productsError && <p className="products-error" role="alert">{productsError}</p>}

        <div className="products-table-wrap">
          <table className="products-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Producto</th>
                <th>Slug</th>
                <th>Costo</th>
                <th>Existencias</th>
                <th>Destacado</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>#{product.id}</td>
                  <td>{product.nombre}</td>
                  <td>{product.slug}</td>
                  <td>{formatCost(product.costo)}</td>
                  <td>{product.existencias}</td>
                  <td>{product.destacado ? 'Sí' : 'No'}</td>
                </tr>
              ))}
              {!loadingProducts && products.length === 0 && !productsError && (
                <tr>
                  <td className="empty-products" colSpan="6">Todavía no hay productos registrados.</td>
                </tr>
              )}
              {loadingProducts && (
                <tr>
                  <td className="empty-products" colSpan="6">Consultando SQL Server…</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
      </>}

      <footer className="footer">
        <span>ERIK’S CREACIONES</span>
        <span>Diseñado para gestionar cada detalle</span>
      </footer>
      </div>
      </div>
    </main>
  );
}

function HomePanel({ onNavigate }) {
  return (
    <section className="home-page">
      <div className="home-welcome">
        <span className="eyebrow">BIENVENIDA A TU ESPACIO</span>
        <h1>Hola, Erik’s Creaciones <span>♡</span></h1>
        <p>Administra tus ventas y productos desde un solo lugar.</p>
      </div>
      <div className="home-shortcuts">
        <button type="button" className="home-shortcut-card" onClick={() => onNavigate('ventas')}>
          <span className="shortcut-icon">▧</span><strong>Crear una venta</strong><span>Abre una comanda y agrega productos.</span><i>→</i>
        </button>
        <button type="button" className="home-shortcut-card" onClick={() => onNavigate('productos')}>
          <span className="shortcut-icon">◇</span><strong>Ver inventario</strong><span>Consulta los productos registrados.</span><i>→</i>
        </button>
      </div>
      <div className="home-note"><span>♡</span><p><strong>Hecho con cariño</strong><br />Cada pedido cuenta una historia. Gracias por crear con Erik’s.</p></div>
    </section>
  );
}

function InformationPanel({ title, description, action, onAction }) {
  return (
    <section className="information-panel">
      <span className="eyebrow">ERIK’S CREACIONES</span>
      <h1>{title}</h1>
      <p>{description}</p>
      <button className="button-primary" type="button" onClick={onAction}>{action} <span>→</span></button>
    </section>
  );
}

function formatCost(value) {
  return new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN',
  }).format(Number(value));
}

function slugify(value) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 100);
}

export default App;

import { useEffect, useRef, useState } from 'react';

const emptyForm = {
  nombre: '',
  slug: '',
  descripcion: '',
  imagenUrl: '',
  id: '',
  documentoId: '',
  rol: '',
  telefono: '',
  correo: '',
  contrasena: '',
  direccion: '',
  ciudadId: '',
  fechaNacimiento: '',
  usuarioId: '',
  productoId: '',
};

const titles = {
  ventas: 'Ventas',
  empleados: 'Empleados',
  categorias: 'Categorías',
  colores: 'Colores por producto',
  tallas: 'Tallas por producto',
  usuarios: 'Usuarios',
  insumos: 'Insumos menores',
};

function ManagementPanel({ view, onNavigate }) {
  const [records, setRecords] = useState([]);
  const [options, setOptions] = useState([]);
  const [cities, setCities] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [selectedSaleId, setSelectedSaleId] = useState('');
  const [saleDetail, setSaleDetail] = useState({ items: [], total: 0 });
  const [saleProductId, setSaleProductId] = useState('');
  const [saleQuantity, setSaleQuantity] = useState('1');
  const [savingItem, setSavingItem] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  if (view === 'ventas') {
    return <SalesPanel onNavigate={onNavigate} />;
  }

  async function load() {
    setLoading(true);
    setError('');
    try {
      if (view === 'ventas') {
        const [sales, users, saleProducts] = await Promise.all([
          window.inventario.obtenerRegistros('ventas'),
          window.inventario.obtenerRegistros('usuarios'),
          window.inventario.obtenerRegistros('productosVenta'),
        ]);
        setRecords(sales);
        setOptions(users);
        setProducts(saleProducts);
        setSelectedSaleId((current) => (
          sales.some((sale) => String(sale.id) === String(current))
            ? String(current)
            : ''
        ));
        setSaleProductId((current) => (
          saleProducts.some((product) => String(product.id) === String(current))
            ? String(current)
            : String(saleProducts[0]?.id || '')
        ));
      } else if (view === 'empleados') {
        const [employees, documents] = await Promise.all([
          window.inventario.obtenerRegistros('empleados'),
          window.inventario.obtenerRegistros('documentos'),
        ]);
        setRecords(employees);
        setOptions(documents);
      } else if (view === 'usuarios') {
        const [users, catalogs] = await Promise.all([
          window.inventario.obtenerRegistros('usuariosCompletos'),
          window.inventario.obtenerRegistros('catalogosUsuario'),
        ]);
        setRecords(users);
        setOptions(catalogs.documentos);
        setCities(catalogs.ciudades);
      } else if (view === 'categorias') {
        setRecords(await window.inventario.obtenerRegistros('categorias'));
      } else if (view === 'colores' || view === 'tallas') {
        const productList = await window.inventario.obtenerRegistros('productos');
        setProducts(productList);
        setSelectedProductId((current) => (
          productList.some((product) => String(product.id) === String(current))
            ? String(current)
            : String(productList[0]?.id || '')
        ));
      }
    } catch (loadError) {
      setError(loadError.message || `No se pudieron cargar ${titles[view].toLowerCase()}.`);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (view === 'ventas') return;
    setRecords([]);
    setOptions([]);
    setForm(emptyForm);
    setNotice(null);
    void load();
  }, [view]);

  useEffect(() => {
    if (view !== 'colores' && view !== 'tallas') return;
    if (!selectedProductId) {
      setRecords([]);
      return;
    }
    let active = true;
    window.inventario.listarVariantes(view, Number(selectedProductId))
      .then((items) => {
        if (active) setRecords(items);
      })
      .catch((loadError) => {
        if (active) setError(loadError.message || `No se pudieron cargar las ${view}.`);
      });
    return () => {
      active = false;
    };
  }, [view, selectedProductId]);

  useEffect(() => {
    if (view !== 'ventas' || !selectedSaleId) {
      setSaleDetail({ items: [], total: 0 });
      return;
    }
    let active = true;
    setSaleDetail({ items: [], total: 0 });
    setError('');
    window.inventario.obtenerDetalleVenta(Number(selectedSaleId))
      .then((detail) => {
        if (active) setSaleDetail(detail);
      })
      .catch((loadError) => {
        if (active) setError(loadError.message || 'No se pudo cargar el detalle de la venta.');
      });
    return () => {
      active = false;
    };
  }, [view, selectedSaleId]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => {
      const next = { ...current, [name]: value };
      if (name === 'nombre' && view === 'categorias'
        && (!current.slug || current.slug === slugify(current.nombre))) {
        next.slug = slugify(value);
      }
      return next;
    });
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const payload = { ...form };
    if (view === 'ventas') payload.usuarioId = form.usuarioId ? Number(form.usuarioId) : null;
    if (view === 'empleados') {
      payload.id = Number(form.id);
      payload.documentoId = Number(form.documentoId);
    }
    if (view === 'usuarios') {
      payload.id = Number(form.id);
      payload.documentoId = Number(form.documentoId);
      payload.ciudadId = Number(form.ciudadId);
    }
    if (view === 'colores' || view === 'tallas') {
      payload.productoId = Number(selectedProductId);
      payload.nombre = form.nombre.trim();
    }
    for (const key of [
      'nombre', 'slug', 'descripcion', 'imagenUrl', 'rol', 'telefono', 'correo',
      'contrasena', 'direccion',
    ]) {
      if (typeof payload[key] === 'string') payload[key] = payload[key].trim();
    }
    if (view === 'categorias' && !payload.imagenUrl) {
      setNotice({ type: 'error', text: 'La imagen es obligatoria porque así lo requiere la tabla Categorias.' });
      return;
    }
    if (view === 'empleados' && (!payload.id || !payload.documentoId
      || !payload.nombre || !payload.rol || !payload.correo || !payload.contrasena)) {
      setNotice({ type: 'error', text: 'Completa identificación, tipo de documento, nombre, rol, correo y contraseña.' });
      return;
    }
    if (view === 'usuarios' && (!payload.id || !payload.documentoId || !payload.ciudadId
      || !payload.nombre || !payload.direccion || !payload.telefono || !payload.correo
      || !payload.fechaNacimiento)) {
      setNotice({ type: 'error', text: 'Completa todos los campos requeridos para registrar al usuario.' });
      return;
    }
    if ((view === 'colores' || view === 'tallas') && (!payload.productoId || !payload.nombre)) {
      setNotice({ type: 'error', text: 'Selecciona un producto e ingresa el nombre.' });
      return;
    }

    setSaving(true);
    try {
      const created = await window.inventario.crearRegistro(view, payload);
      if (view === 'ventas') setSelectedSaleId(String(created.id));
      setNotice({
        type: 'success',
        text: view === 'ventas'
          ? `Comanda #${created.id} creada. Ahora puedes añadirle productos.`
          : `${titles[view]} guardado correctamente.`,
      });
      setForm(emptyForm);
      await load();
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo guardar el registro.' });
    } finally {
      setSaving(false);
    }
  }

  async function addItemToSale(event) {
    event.preventDefault();
    setNotice(null);
    const productoId = Number(saleProductId);
    const cantidad = Number(saleQuantity);
    if (!selectedSaleId) {
      setNotice({ type: 'error', text: 'Primero crea o selecciona una comanda.' });
      return;
    }
    if (!productoId || !Number.isInteger(cantidad) || cantidad <= 0) {
      setNotice({ type: 'error', text: 'Selecciona un producto e ingresa una cantidad entera mayor que cero.' });
      return;
    }
    setSavingItem(true);
    try {
      setSaleDetail(await window.inventario.agregarProductoAVenta(
        Number(selectedSaleId),
        { productoId, cantidad },
      ));
      setSaleQuantity('1');
      setNotice({ type: 'success', text: 'Producto agregado a la comanda.' });
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo añadir el producto.' });
    } finally {
      setSavingItem(false);
    }
  }

  if (view === 'insumos') {
    return (
      <section className="management-card" aria-labelledby="management-title">
        <span className="eyebrow">HU5 · PENDIENTE POR ESQUEMA</span>
        <h1 id="management-title">{titles[view]}</h1>
        <p>El esquema compartido no contiene una tabla Insumos. No es posible guardar nombre, referencia, unidad, costo y cantidad sin modificar la base.</p>
        <p className="schema-warning">La base de datos se mantiene intacta; este módulo no realiza escrituras.</p>
      </section>
    );
  }

  const variantView = view === 'colores' || view === 'tallas';
  const nameLabel = view === 'colores' ? 'Nombre del color' : 'Talla';

  return (
    <section className="management-layout" aria-labelledby="management-title">
      <div className="management-card">
        <span className="eyebrow">GESTIÓN · HU{view === 'ventas' ? '2' : view === 'empleados' ? '3' : view === 'categorias' ? '6' : view === 'colores' ? '7' : '8'}</span>
        <h1 id="management-title">{titles[view]}</h1>
        {view === 'empleados' && (
          <p className="schema-warning">
            La contraseña se guarda como hash. Ejecuta la consulta SQL indicada al finalizar para agregar la columna requerida en SQL Server.
          </p>
        )}
        {view === 'usuarios' && <p>Datos solicitados conforme a las columnas de la tabla Usuarios; no se agrega rol ni contraseña.</p>}
        {view === 'colores' && <p>Los colores se asocian a un producto, como establece la tabla Colores.</p>}
        {view === 'tallas' && <p>Las tallas se asocian a un producto, como establece la tabla Tallas.</p>}

        <form className="management-form" onSubmit={submit}>
          {view === 'empleados' && (
            <>
              <label className="field"><span>ID (documento)</span><input name="id" value={form.id} onChange={updateField} inputMode="numeric" required /></label>
              <label className="field">
                <span>Tipo de documento</span>
                <select name="documentoId" value={form.documentoId} onChange={updateField} required>
                  <option value="">Selecciona un tipo</option>
                  {options.map((doc) => <option key={doc.id} value={doc.id}>{doc.nombre}</option>)}
                </select>
              </label>
              <label className="field"><span>Nombre</span><input name="nombre" value={form.nombre} onChange={updateField} maxLength="100" required /></label>
              <label className="field"><span>Rol</span><input name="rol" value={form.rol} onChange={updateField} maxLength="50" placeholder="Producción, Ventas o Jefe" required /></label>
              <label className="field"><span>Correo</span><input name="correo" type="email" value={form.correo} onChange={updateField} maxLength="100" required /></label>
              <label className="field"><span>Teléfono <span className="optional">· opcional</span></span><input name="telefono" value={form.telefono} onChange={updateField} maxLength="20" /></label>
              <label className="field"><span>Contraseña</span><input name="contrasena" type="password" value={form.contrasena} onChange={updateField} minLength="8" autoComplete="new-password" required /></label>
            </>
          )}
          {view === 'usuarios' && (
            <>
              <label className="field"><span>ID (documento)</span><input name="id" type="number" min="100000" max="2147483647" value={form.id} onChange={updateField} required /></label>
              <label className="field">
                <span>Tipo de documento</span>
                <select name="documentoId" value={form.documentoId} onChange={updateField} required>
                  <option value="">Selecciona un tipo</option>
                  {options.map((doc) => <option key={doc.id} value={doc.id}>{doc.nombre}</option>)}
                </select>
              </label>
              <label className="field">
                <span>Ciudad</span>
                <select name="ciudadId" value={form.ciudadId} onChange={updateField} required>
                  <option value="">Selecciona una ciudad</option>
                  {cities.map((city) => <option key={city.id} value={city.id}>{city.nombre}</option>)}
                </select>
              </label>
              <label className="field"><span>Nombre</span><input name="nombre" value={form.nombre} onChange={updateField} maxLength="100" required /></label>
              <label className="field"><span>Dirección</span><input name="direccion" value={form.direccion} onChange={updateField} maxLength="150" required /></label>
              <label className="field"><span>Teléfono</span><input name="telefono" value={form.telefono} onChange={updateField} maxLength="20" required /></label>
              <label className="field"><span>Correo</span><input name="correo" type="email" value={form.correo} onChange={updateField} maxLength="100" required /></label>
              <label className="field"><span>Fecha de nacimiento</span><input name="fechaNacimiento" type="date" value={form.fechaNacimiento} onChange={updateField} required /></label>
            </>
          )}
          {view === 'categorias' && (
            <>
              <label className="field"><span>Nombre</span><input name="nombre" value={form.nombre} onChange={updateField} maxLength="100" required /></label>
              <label className="field"><span>Slug</span><input name="slug" value={form.slug} onChange={updateField} maxLength="250" required /></label>
              <label className="field"><span>Descripción <span className="optional">· opcional</span></span><textarea name="descripcion" rows="3" value={form.descripcion} onChange={updateField} /></label>
              <label className="field"><span>URL de imagen <span className="optional">· requerida por la BD</span></span><input name="imagenUrl" type="url" value={form.imagenUrl} onChange={updateField} maxLength="255" required /></label>
            </>
          )}
          {variantView && (
            <>
              <label className="field">
                <span>Producto</span>
                <select value={selectedProductId} onChange={(event) => setSelectedProductId(event.target.value)} required>
                  <option value="">Selecciona un producto</option>
                  {products.map((product) => <option key={product.id} value={product.id}>{product.nombre} · #{product.id}</option>)}
                </select>
              </label>
              <label className="field"><span>{nameLabel}</span><input name="nombre" value={form.nombre} onChange={updateField} maxLength="50" required /></label>
            </>
          )}
          {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
          <button type="submit" disabled={saving || loading || (variantView && !products.length) || (view === 'usuarios' && (!options.length || !cities.length))}>
            {saving ? 'Guardando…' : `Crear ${view === 'usuarios' ? 'usuario' : view === 'categorias' ? 'categoría' : view === 'empleados' ? 'empleado' : view === 'colores' ? 'color' : 'talla'}`}
          </button>
        </form>
      </div>

      <div className="management-card">
        <div className="management-list-heading">
          <div>
            <span className="eyebrow">REGISTROS EN LA BASE</span>
            <h2>{view === 'ventas' ? 'Comandas' : `${titles[view]} registrados`}</h2>
          </div>
          <button className="refresh-button" type="button" onClick={() => void load()} disabled={loading}>
            {loading ? 'Cargando…' : 'Actualizar'}
          </button>
        </div>
        {error && <p className="products-error" role="alert">{error}</p>}
        {loading ? <p>Consultando base de datos…</p> : (
          <div className="management-list">
            {records.length === 0 && <p>No hay registros para mostrar.</p>}
            {records.map((record) => (
              <div className="management-record" key={record.id}>
                <strong>#{record.id} · {record.nombre || (view === 'ventas' ? 'Comanda' : '')}</strong>
                <span>
                  {view === 'ventas' && `${record.usuarioId ? `Usuario #${record.usuarioId}` : 'Sin cliente'} · ${record.estado}`}
                  {view === 'empleados' && `${record.rol} · ${record.correo}`}
                  {view === 'usuarios' && `${record.correo} · ${record.telefono}`}
                  {view === 'categorias' && record.slug}
                  {view === 'colores' && `Producto #${record.productoId}`}
                  {view === 'tallas' && `Producto #${record.productoId}`}
                </span>
                {view === 'ventas' && (
                  <button
                    className="refresh-button"
                    type="button"
                    aria-pressed={selectedSaleId === String(record.id)}
                    onClick={() => setSelectedSaleId(String(record.id))}
                  >
                    {selectedSaleId === String(record.id) ? 'Comanda abierta' : 'Abrir comanda'}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
        {view === 'ventas' && selectedSaleId && (
          <section className="sale-cart" aria-labelledby="sale-cart-title">
            <span className="eyebrow">COMANDA ABIERTA · #{selectedSaleId}</span>
            <h2 id="sale-cart-title">Productos de la venta</h2>
            <form className="sale-item-form" onSubmit={addItemToSale}>
              <label className="field">
                <span>Producto</span>
                <select
                  value={saleProductId}
                  onChange={(event) => setSaleProductId(event.target.value)}
                  required
                >
                  <option value="">Selecciona un producto</option>
                  {products.map((product) => (
                    <option key={product.id} value={product.id}>
                      {product.nombre} · {formatMoney(product.costo)} · stock {product.existencias}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Cantidad</span>
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={saleQuantity}
                  onChange={(event) => setSaleQuantity(event.target.value)}
                  required
                />
              </label>
              <button type="submit" disabled={savingItem || !products.length}>
                {savingItem ? 'Añadiendo…' : 'Añadir producto'}
              </button>
            </form>
            <p className="schema-warning">
              El valor unitario usa Productos.Costo, que es el importe disponible en el esquema. Añadir a la comanda no descuenta existencias; aquí aún no se registra el cierre de venta ni la salida física.
            </p>
            <div className="management-list">
              {saleDetail.items.length === 0 && <p>La comanda está vacía. Añade productos para continuar.</p>}
              {saleDetail.items.map((item) => (
                <div className="management-record" key={item.productoId}>
                  <strong>{item.nombre} · {item.cantidad} × {formatMoney(item.costo)}</strong>
                  <span>Subtotal {formatMoney(item.subtotal)}</span>
                </div>
              ))}
              <div className="sale-total">
                <strong>Total de la comanda</strong>
                <strong>{formatMoney(saleDetail.total)}</strong>
              </div>
            </div>
          </section>
        )}
      </div>
    </section>
  );
}

function SalesPanel({ onNavigate }) {
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [sales, setSales] = useState([]);
  const [customerId, setCustomerId] = useState('');
  const [saleId, setSaleId] = useState('');
  const [search, setSearch] = useState('');
  const [detail, setDetail] = useState({ items: [], total: 0 });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [busyProduct, setBusyProduct] = useState(null);
  const [notice, setNotice] = useState(null);
  const [error, setError] = useState('');
  const searchInputRef = useRef(null);
  const today = (() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  })();

  async function loadSales() {
    setLoading(true);
    setError('');
    if (!window.inventario) {
      setLoading(false);
      return;
    }
    try {
      const [customerOptions, productOptions, saleList] = await Promise.all([
        window.inventario.obtenerRegistros('usuarios'),
        window.inventario.obtenerRegistros('productosVenta'),
        window.inventario.obtenerRegistros('ventas'),
      ]);
      setCustomers(customerOptions);
      setProducts(productOptions);
      setSales(saleList.filter((sale) => sale.estado?.toLowerCase() === 'generado'));
    } catch (loadError) {
      setError(loadError.message || 'No se pudo cargar la información para la venta.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadSales();
  }, []);

  useEffect(() => {
    if (!saleId) {
      setDetail({ items: [], total: 0 });
      return;
    }
    let active = true;
    window.inventario.obtenerDetalleVenta(Number(saleId))
      .then((saleDetail) => {
        if (active) setDetail(saleDetail);
      })
      .catch((loadError) => {
        if (active) setError(loadError.message || 'No se pudo cargar la comanda.');
      });
    return () => {
      active = false;
    };
  }, [saleId]);

  const selectedSale = sales.find((sale) => String(sale.id) === saleId);
  const filteredProducts = products.filter((product) => (
    product.nombre.toLocaleLowerCase().includes(search.trim().toLocaleLowerCase())
  ));
  const iva = Number((detail.total * 0.19).toFixed(2));
  const total = Number((detail.total + iva).toFixed(2));

  async function createPendingSale() {
    setNotice(null);
    setSaving(true);
    let createdSale;
    try {
      createdSale = await window.inventario.crearRegistro('ventas', {
        usuarioId: customerId ? Number(customerId) : null,
      });
      setSales((current) => [createdSale, ...current]);
      let savedDetail = { items: [], total: 0 };
      for (const item of detail.items) {
        savedDetail = await window.inventario.agregarProductoAVenta(createdSale.id, {
          productoId: item.productoId,
          cantidad: item.cantidad,
        });
        setDetail(savedDetail);
      }
      setSaleId(String(createdSale.id));
      setNotice({ type: 'success', text: `Venta #${createdSale.id} guardada como pendiente.` });
    } catch (saveError) {
      if (createdSale) {
        setSaleId(String(createdSale.id));
      }
      setNotice({ type: 'error', text: saveError.message || 'No se pudo crear la venta.' });
    } finally {
      setSaving(false);
    }
  }

  async function addProduct(productId, quantity = 1) {
    setNotice(null);
    setBusyProduct(productId);
    try {
      if (!saleId) {
        const product = products.find((item) => item.id === Number(productId));
        if (!product) throw new Error('El producto seleccionado ya no está disponible.');
        setDetail((current) => {
          const existing = current.items.find((item) => item.productoId === product.id);
          const items = existing
            ? current.items.map((item) => item.productoId === product.id
              ? { ...item, cantidad: item.cantidad + quantity, subtotal: item.costo * (item.cantidad + quantity) }
              : item)
            : [...current.items, {
              productoId: product.id,
              nombre: product.nombre,
              cantidad: quantity,
              costo: Number(product.costo),
              subtotal: Number(product.costo) * quantity,
            }];
          return {
            items,
            total: items.reduce((sum, item) => sum + item.subtotal, 0),
          };
        });
        setNotice({ type: 'success', text: 'Producto agregado a la comanda.' });
      } else {
        setDetail(await window.inventario.agregarProductoAVenta(
          Number(saleId),
          { productoId: Number(productId), cantidad: quantity },
        ));
        setNotice({ type: 'success', text: 'Producto agregado a la venta pendiente.' });
      }
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo agregar el producto.' });
    } finally {
      setBusyProduct(null);
    }
  }

  async function changeQuantity(productId, quantity) {
    setNotice(null);
    setBusyProduct(productId);
    try {
      if (!saleId) {
        setDetail((current) => {
          const items = quantity <= 0
            ? current.items.filter((item) => item.productoId !== productId)
            : current.items.map((item) => item.productoId === productId
              ? { ...item, cantidad: quantity, subtotal: item.costo * quantity }
              : item);
          return { items, total: items.reduce((sum, item) => sum + item.subtotal, 0) };
        });
      } else if (quantity <= 0) {
        setDetail(await window.inventario.quitarProductoDeVenta(Number(saleId), productId));
      } else {
        setDetail(await window.inventario.actualizarCantidadVenta(Number(saleId), productId, quantity));
      }
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo actualizar la cantidad.' });
    } finally {
      setBusyProduct(null);
    }
  }

  return (
    <div className="sales-page">
      <header className="page-header">
        <div>
          <div className="breadcrumbs"><span>Ventas</span><span>/</span><span>Nueva venta</span></div>
          <h1>Crear venta</h1>
        </div>
        <div className="page-actions">
          <button className="button-secondary" type="button" onClick={() => onNavigate('productos')}>Cancelar</button>
          <button className="button-primary" type="button" onClick={createPendingSale} disabled={saving || loading || Boolean(selectedSale)}>
            {saving ? 'Guardando…' : selectedSale ? 'Venta guardada' : 'Guardar venta'}
          </button>
        </div>
      </header>

      <div className="sales-grid">
        <div className="sales-main-column">
          <section className="sale-section-card">
            <div className="sale-section-title">
              <div>
                <span className="section-kicker">01 · INFORMACIÓN</span>
                <h2>Detalles de la venta</h2>
                <p>El cliente es opcional; puedes preparar una comanda pendiente.</p>
              </div>
              <span className="section-icon" aria-hidden="true">♙</span>
            </div>
            <div className="sale-fields">
              <label className="field">
                <span>Cliente <span className="muted-inline">· opcional</span></span>
                <select
                  value={selectedSale ? (selectedSale.usuarioId ?? '') : customerId}
                  onChange={(event) => setCustomerId(event.target.value)}
                  disabled={loading || Boolean(selectedSale)}
                >
                  <option value="">Sin cliente</option>
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>{customer.nombre}</option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Fecha de venta <span className="muted-inline">· se asigna al guardar</span></span>
                <input type="date" value={selectedSale?.fechaCreacion?.slice(0, 10) || today} readOnly />
              </label>
            </div>
            {sales.length > 0 && (
              <label className="field existing-sale-field">
                <span>Continuar una venta pendiente</span>
                <select value={saleId} onChange={(event) => {
                  const id = event.target.value;
                  setSaleId(id);
                  const sale = sales.find((item) => String(item.id) === id);
                  setCustomerId(sale?.usuarioId ? String(sale.usuarioId) : '');
                }}>
                  <option value="">Nueva venta</option>
                  {sales.map((sale) => (
                    <option key={sale.id} value={sale.id}>
                      Venta #{sale.id} · {sale.usuarioId ? `cliente #${sale.usuarioId}` : 'sin cliente'} · {sale.estado}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </section>

          <section className="sale-section-card sale-products-card">
            <div className="sale-products-header">
              <div>
                <span className="section-kicker">02 · PRODUCTOS</span>
                <h2>Productos de la venta</h2>
              </div>
              <label className="product-search">
                <span className="search-icon" aria-hidden="true">⌕</span>
                <input
                  ref={searchInputRef}
                  type="search"
                  placeholder="Buscar producto"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </label>
            </div>
            <div className="quick-products" aria-label="Agregar productos rápidamente">
              {filteredProducts.slice(0, 5).map((product) => (
                <button
                  className="quick-product-chip"
                  key={product.id}
                  type="button"
                  onClick={() => void addProduct(product.id)}
                  disabled={busyProduct === product.id}
                  title={`Agregar ${product.nombre}`}
                >
                  <span className="product-color-dot" />
                  {product.nombre}
                  <span className="chip-plus">+</span>
                </button>
              ))}
              {products.length === 0 && !loading && <span className="empty-inline">No hay productos disponibles.</span>}
            </div>
            {error && <p className="products-error" role="alert">{error}</p>}
            <div className="sale-table-scroll">
              <table className="sale-items-table">
                <thead>
                  <tr><th>Producto</th><th>Precio</th><th>Cantidad</th><th>Subtotal</th><th aria-label="Acciones" /></tr>
                </thead>
                <tbody>
                  {detail.items.map((item) => {
                    const product = products.find((entry) => entry.id === item.productoId);
                    return (
                      <tr key={item.productoId}>
                        <td>
                          <div className="sale-product-name">
                            <span className="product-thumb" aria-hidden="true">◇</span>
                            <span><strong>{item.nombre}</strong><small>{product?.existencias ?? '—'} disponibles</small></span>
                          </div>
                        </td>
                        <td>{formatMoney(item.costo)}</td>
                        <td>
                          <div className="quantity-control">
                            <button type="button" aria-label={`Quitar una unidad de ${item.nombre}`} disabled={busyProduct === item.productoId} onClick={() => void changeQuantity(item.productoId, item.cantidad - 1)}>−</button>
                            <span>{item.cantidad}</span>
                            <button type="button" aria-label={`Agregar una unidad de ${item.nombre}`} disabled={busyProduct === item.productoId} onClick={() => void changeQuantity(item.productoId, item.cantidad + 1)}>+</button>
                          </div>
                        </td>
                        <td className="sale-line-total">{formatMoney(item.subtotal)}</td>
                        <td>
                          <button className="remove-item-button" type="button" aria-label={`Quitar ${item.nombre}`} disabled={busyProduct === item.productoId} onClick={() => void changeQuantity(item.productoId, 0)}>×</button>
                        </td>
                      </tr>
                    );
                  })}
                  {detail.items.length === 0 && (
                    <tr><td className="sale-empty-state" colSpan="5">
                      Agrega productos desde la búsqueda para preparar la comanda.
                    </td></tr>
                  )}
                </tbody>
              </table>
            </div>
            <button className="add-product-link" type="button" onClick={() => searchInputRef.current?.focus()}>
              <span>＋</span> Agregar producto
            </button>
          </section>
          {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
        </div>

        <aside className="sale-summary-column">
          <section className="sale-summary-card">
            <div className="summary-title">
              <h2>Resumen</h2>
              <span className="items-badge">{detail.items.reduce((count, item) => count + item.cantidad, 0)} productos</span>
            </div>
            <div className="summary-row"><span>Subtotal</span><strong>{formatMoney(detail.total)}</strong></div>
            <div className="summary-row"><span>IVA (19%)</span><strong>{formatMoney(iva)}</strong></div>
            <div className="summary-total">
              <span>Total a cobrar<small>Impuestos incluidos</small></span>
              <strong>{formatMoney(total)}</strong>
            </div>
            <div className="pending-info">
              <span aria-hidden="true">▤</span>
              <div><strong>Se guardará como pendiente</strong><p>Guardar la comanda no descuenta las existencias del inventario.</p></div>
            </div>
            <button className="save-pending-button" type="button" onClick={createPendingSale} disabled={saving || loading || Boolean(selectedSale)}>
              {saving ? 'Guardando…' : selectedSale ? 'Venta pendiente guardada' : 'Guardar como pendiente'}
              <span aria-hidden="true">›</span>
            </button>
            <p className="summary-footnote">
              {selectedSale ? `Puedes continuar editando la venta #${selectedSale.id}.` : 'La venta y sus productos se guardarán juntos como pendientes.'}
            </p>
          </section>
          {selectedSale && (
            <div className="sale-success-card">
              <strong>Venta creada correctamente</strong>
              <p>La venta #{selectedSale.id} quedó pendiente y aún no se ha descontado inventario.</p>
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}

function formatMoney(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
  }).format(Number(value));
}

function slugify(value) {
  return value.normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 250);
}

export default ManagementPanel;

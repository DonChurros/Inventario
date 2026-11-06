import { useEffect, useMemo, useState } from 'react';

const initialForm = {
  codigo: '',
  nombre: '',
  sistema: 'Adulto',
  ordenVisual: '',
  estado: 'Activo',
};

function sizeCode(id) {
  return `TAL-${String(id).padStart(3, '0')}`;
}

function SizesPanel() {
  const [sizes, setSizes] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [assigningId, setAssigningId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [sizeList, productList] = await Promise.all([
        window.inventario.obtenerRegistros('tallas'),
        window.inventario.obtenerRegistros('productos'),
      ]);
      setSizes(sizeList);
      setProducts(productList);
      setSelectedProductId((current) => (
        productList.some((product) => String(product.id) === current)
          ? current
          : String(productList[0]?.id || '')
      ));
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar las tallas.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const activeCount = sizes.filter((size) => size.estado === 'Activo').length;
  const systems = useMemo(() => [...new Set(sizes.map((size) => size.sistema))], [sizes]);
  const filteredSizes = useMemo(() => {
    const query = search.trim().toLocaleLowerCase();
    return sizes.filter((size) => (
      !query || [size.codigo, size.nombre, size.sistema, sizeCode(size.id)]
        .some((value) => value.toLocaleLowerCase().includes(query))
    ));
  }, [sizes, search]);
  const groupedSizes = useMemo(() => {
    const grouped = new Map();
    filteredSizes.forEach((size) => {
      const system = size.sistema || 'Sin definir';
      if (!grouped.has(system)) grouped.set(system, []);
      grouped.get(system).push(size);
    });
    return [...grouped.entries()];
  }, [filteredSizes]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const payload = {
      codigo: form.codigo.trim().toUpperCase(),
      nombre: form.nombre.trim(),
      sistema: form.sistema,
      ordenVisual: Number(form.ordenVisual),
      estado: form.estado,
    };
    if (!payload.codigo || !payload.nombre || !Number.isInteger(payload.ordenVisual)
      || payload.ordenVisual < 0) {
      setNotice({ type: 'error', text: 'Completa el código, el nombre y un orden visual entero igual o mayor que cero.' });
      return;
    }

    setSaving(true);
    try {
      const created = await window.inventario.crearRegistro('tallas', payload);
      setSizes((current) => [...current, created].sort((a, b) => (
        a.sistema.localeCompare(b.sistema)
        || a.ordenVisual - b.ordenVisual
        || a.nombre.localeCompare(b.nombre)
      )));
      setForm(initialForm);
      setNotice({ type: 'success', text: 'Talla creada correctamente.' });
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo crear la talla.' });
    } finally {
      setSaving(false);
    }
  }

  async function assignSize(size) {
    if (!selectedProductId) return;
    setAssigningId(size.id);
    setNotice(null);
    try {
      const updated = await window.inventario.asignarTallaAProducto(
        Number(selectedProductId),
        size.id,
      );
      setSizes((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setNotice({ type: 'success', text: `Talla asignada a ${products.find((product) => String(product.id) === selectedProductId)?.nombre || 'el producto'}.` });
    } catch (assignError) {
      setNotice({ type: 'error', text: assignError.message || 'No se pudo asignar la talla.' });
    } finally {
      setAssigningId(null);
    }
  }

  return (
    <section className="sizes-page" aria-labelledby="sizes-title">
      <header className="sizes-heading">
        <div>
          <span className="eyebrow">CARACTERÍSTICAS DE PRODUCTO</span>
          <h1 id="sizes-title">Crear nueva talla</h1>
          <p>Define escalas consistentes para controlar cada variante de producto.</p>
        </div>
        <div className="size-summary">
          <div><span>Tallas</span><strong>{sizes.length}</strong></div>
          <div className="active"><span>Activas</span><strong>{activeCount}</strong></div>
        </div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="sizes-grid">
        <section className="size-card size-form-card">
          <header className="size-card-heading">
            <span className="size-heading-icon" aria-hidden="true">⌁</span>
            <div><h2>Información de talla</h2><p>Todos los campos son obligatorios</p></div>
          </header>
          <form className="size-form" onSubmit={submit}>
            <div className="size-field-row">
              <label className="field">
                <span>ID de talla</span>
                <input value="Asignado automáticamente" readOnly aria-describedby="size-id-hint" />
              </label>
              <label className="field">
                <span>Código corto</span>
                <input name="codigo" maxLength="20" placeholder="Ej. XXL" value={form.codigo} onChange={updateField} required />
              </label>
            </div>
            <span className="size-id-hint" id="size-id-hint">El código TAL se genera al crear el registro.</span>
            <label className="field">
              <span>Nombre descriptivo</span>
              <input name="nombre" maxLength="100" placeholder="Ej. Doble extra grande" value={form.nombre} onChange={updateField} required />
            </label>
            <div className="size-field-row">
              <label className="field">
                <span>Sistema de tallaje</span>
                <select name="sistema" value={form.sistema} onChange={updateField} required>
                  <option value="Adulto">Adulto</option>
                  <option value="Infantil">Infantil</option>
                  <option value="Sin definir">Sin definir</option>
                </select>
              </label>
              <label className="field">
                <span>Orden visual</span>
                <input name="ordenVisual" type="number" min="0" step="1" placeholder="Ej. 6" value={form.ordenVisual} onChange={updateField} required />
              </label>
            </div>
            <label className="field">
              <span>Estado inicial</span>
              <select name="estado" value={form.estado} onChange={updateField} required>
                <option value="Activo">Activa</option>
                <option value="Inactivo">Inactiva</option>
              </select>
            </label>
            <div className="size-preview">
              <span className="size-preview-code">{form.codigo.trim() || '—'}</span>
              <span><small>VISTA PREVIA</small><strong>{form.nombre.trim() || 'Nueva talla'}</strong><span>{form.sistema || 'Sin sistema definido'}</span></span>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="size-submit" type="submit" disabled={saving || loading || !form.codigo.trim() || !form.nombre.trim()}>
              {saving ? 'Creando…' : '＋　Crear talla'}
            </button>
          </form>
        </section>

        <div className="sizes-side">
          <section className="size-card size-scales-card">
            <header className="size-scales-heading">
              <div><h2>Escalas disponibles</h2><p>Orden de las tallas visible al registrar productos</p></div>
              <span>{systems.length} {systems.length === 1 ? 'sistema' : 'sistemas'}</span>
            </header>
            <div className="size-scales">
              {groupedSizes.map(([system, items]) => (
                <section key={system}>
                  <header><strong>{system.toLocaleUpperCase()}</strong><span>{items.length} {items.length === 1 ? 'talla' : 'tallas'}</span></header>
                  <div>{items.map((size) => (
                    <span className={`size-chip ${size.estado === 'Inactivo' ? 'inactive' : ''}`} key={size.id} title={`${size.nombre} · orden ${size.ordenVisual}`}>
                      {size.codigo}
                    </span>
                  ))}</div>
                </section>
              ))}
              {!loading && groupedSizes.length === 0 && <p className="size-empty">No hay escalas registradas.</p>}
            </div>
          </section>

          <section className="size-card size-details-card">
            <header className="size-details-heading">
              <div><h2>Detalle de tallas</h2><p>{sizes.reduce((total, size) => total + size.productos, 0)} asignaciones en productos</p></div>
              <input type="search" aria-label="Buscar talla" placeholder="Buscar talla…" value={search} onChange={(event) => setSearch(event.target.value)} />
            </header>
            <div className="size-assignment-bar">
              <label>
                <span>Asignar a producto</span>
                <select value={selectedProductId} onChange={(event) => setSelectedProductId(event.target.value)} disabled={!products.length}>
                  <option value="">Selecciona un producto</option>
                  {products.map((product) => <option key={product.id} value={product.id}>{product.nombre}</option>)}
                </select>
              </label>
            </div>
            <div className="size-table-scroll">
              <table className="size-table">
                <thead><tr><th>Talla</th><th>Sistema</th><th>Orden</th><th>Productos</th><th>Estado</th><th aria-label="Acciones" /></tr></thead>
                <tbody>
                  {filteredSizes.map((size) => (
                    <tr key={size.id}>
                      <td><span className="size-table-identity"><strong>{size.codigo}</strong><span><b>{size.nombre}</b><small>{sizeCode(size.id)}</small></span></span></td>
                      <td>{size.sistema}</td>
                      <td>{size.ordenVisual}</td>
                      <td><strong>{size.productos}</strong></td>
                      <td><span className={`size-state ${size.estado === 'Activo' ? 'active' : ''}`}>{size.estado === 'Activo' ? 'Activa' : 'Inactiva'}</span></td>
                      <td><button className="size-assign-button" type="button" onClick={() => void assignSize(size)} disabled={size.estado !== 'Activo' || !selectedProductId || assigningId === size.id}>{assigningId === size.id ? '…' : '+'}</button></td>
                    </tr>
                  ))}
                  {!loading && filteredSizes.length === 0 && <tr><td className="size-empty" colSpan="6">No hay tallas para mostrar.</td></tr>}
                  {loading && <tr><td className="size-empty" colSpan="6">Consultando tallas…</td></tr>}
                </tbody>
              </table>
            </div>
            <footer className="size-details-footer"><span>Mostrando {filteredSizes.length} tallas</span><span>{activeCount} disponibles para asignar</span></footer>
          </section>
        </div>
      </div>
    </section>
  );
}

export default SizesPanel;

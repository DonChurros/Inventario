import { useEffect, useMemo, useState } from 'react';

const suggestedColors = ['#2563EB', '#7C3AED', '#DB2777', '#DC2626', '#EA580C', '#CA8A04', '#16A34A', '#0891B2'];
const initialForm = { nombre: '', codigoHex: '#14B8A6', productoId: '' };

function colorCode(id) {
  return `COL-${String(id).padStart(3, '0')}`;
}

function ColorsPanel() {
  const [colors, setColors] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [colorList, productList] = await Promise.all([
        window.inventario.obtenerRegistros('colores'),
        window.inventario.obtenerRegistros('productos'),
      ]);
      setColors(colorList);
      setProducts(productList);
      setForm((current) => ({
        ...current,
        productoId: productList.some((product) => String(product.id) === current.productoId)
          ? current.productoId
          : String(productList[0]?.id || ''),
      }));
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar los colores.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const palette = useMemo(() => {
    const grouped = new Map();
    colors.forEach((color) => {
      const hex = color.codigoHex || '#14B8A6';
      const key = `${color.nombre.toLocaleLowerCase()}|${hex.toUpperCase()}`;
      const current = grouped.get(key);
      if (current) {
        current.productIds.add(color.productoId);
        current.ids.push(color.id);
      } else {
        grouped.set(key, {
          nombre: color.nombre,
          codigoHex: hex,
          id: color.id,
          ids: [color.id],
          productIds: new Set([color.productoId]),
        });
      }
    });
    return [...grouped.values()].sort((a, b) => a.nombre.localeCompare(b.nombre));
  }, [colors]);

  const filteredPalette = useMemo(() => {
    const query = search.trim().toLocaleLowerCase();
    return palette.filter((color) => (
      !query || [color.nombre, color.codigoHex, colorCode(color.id)]
        .some((value) => value.toLocaleLowerCase().includes(query))
    ));
  }, [palette, search]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const nombre = form.nombre.trim();
    if (!nombre || !form.productoId) {
      setNotice({ type: 'error', text: 'Ingresa un nombre y selecciona el producto asociado.' });
      return;
    }
    if (!/^#[0-9a-fA-F]{6}$/.test(form.codigoHex)) {
      setNotice({ type: 'error', text: 'El código debe tener el formato #RRGGBB.' });
      return;
    }

    setSaving(true);
    try {
      const created = await window.inventario.crearRegistro('colores', {
        productoId: Number(form.productoId),
        nombre,
        codigoHex: form.codigoHex.toUpperCase(),
      });
      setColors((current) => [...current, created]);
      setForm((current) => ({ ...initialForm, productoId: current.productoId }));
      setNotice({ type: 'success', text: `Color registrado para ${products.find((product) => String(product.id) === form.productoId)?.nombre || 'el producto'}.` });
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo registrar el color.' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="colors-page" aria-labelledby="colors-title">
      <header className="colors-heading">
        <div>
          <span className="eyebrow">CARACTERÍSTICAS DE PRODUCTO</span>
          <h1 id="colors-title">Crear nuevo color</h1>
          <p>Amplía la paleta disponible para responder a las preferencias del cliente.</p>
        </div>
        <div className="color-summary">
          <div><span>Colores</span><strong>{palette.length}</strong></div>
          <div className="assignments"><span>Asignaciones</span><strong>{colors.length}</strong></div>
        </div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="colors-grid">
        <section className="color-card color-form-card">
          <header className="color-card-heading">
            <span className="color-heading-icon" aria-hidden="true">◉</span>
            <div><h2>Información del color</h2><p>Define un color para asociarlo a un producto</p></div>
          </header>
          <form className="color-form" onSubmit={submit}>
            <div className="color-field-row">
              <label className="field">
                <span>ID del color</span>
                <input value="Asignado automáticamente" readOnly aria-describedby="color-id-hint" />
              </label>
              <label className="field">
                <span>Nombre comercial</span>
                <input name="nombre" maxLength="50" placeholder="Ej. Azul océano" value={form.nombre} onChange={updateField} required />
              </label>
            </div>
            <span className="color-id-hint" id="color-id-hint">El código COL se genera al crear el registro.</span>
            <label className="field">
              <span>Producto asociado</span>
              <select name="productoId" value={form.productoId} onChange={updateField} required>
                <option value="">Selecciona un producto</option>
                {products.map((product) => <option key={product.id} value={product.id}>{product.nombre}</option>)}
              </select>
            </label>
            <label className="field">
              <span>Código de color</span>
              <div className="color-code-input">
                <input aria-label="Selector visual de color" type="color" value={form.codigoHex} onChange={(event) => updateField({ target: { name: 'codigoHex', value: event.target.value } })} />
                <input aria-label="Código hexadecimal" name="codigoHex" maxLength="7" pattern="#[0-9a-fA-F]{6}" value={form.codigoHex} onChange={updateField} required />
              </div>
            </label>
            <div className="color-suggestions">
              <span>Colores sugeridos</span>
              <div>
                {suggestedColors.map((hex) => (
                  <button
                    key={hex}
                    className={form.codigoHex.toUpperCase() === hex ? 'selected' : ''}
                    type="button"
                    aria-label={`Elegir color ${hex}`}
                    title={hex}
                    style={{ '--suggested-color': hex }}
                    onClick={() => setForm((current) => ({ ...current, codigoHex: hex }))}
                  />
                ))}
              </div>
            </div>
            <div className="color-preview">
              <span className="color-preview-swatch" style={{ backgroundColor: /^#[0-9a-fA-F]{6}$/.test(form.codigoHex) ? form.codigoHex : '#14B8A6' }} />
              <span><small>VISTA PREVIA</small><strong>{form.nombre.trim() || 'Nuevo color'}</strong><code>{form.codigoHex.toUpperCase()}</code></span>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="color-submit" type="submit" disabled={saving || loading || !products.length || !form.nombre.trim()}>
              {saving ? 'Creando…' : '＋　Crear color'}
            </button>
          </form>
        </section>

        <section className="color-card color-palette-card">
          <header className="color-palette-heading">
            <div><h2>Paleta de productos</h2><p>{colors.length} asignaciones de color en el catálogo</p></div>
            <input type="search" aria-label="Buscar color" placeholder="Buscar color…" value={search} onChange={(event) => setSearch(event.target.value)} />
          </header>
          <div className="color-palette">
            {filteredPalette.map((color, index) => (
              <article className="color-palette-item" key={`${color.nombre}-${color.codigoHex}`}>
                <div className="color-swatch" style={{ backgroundColor: color.codigoHex }} />
                <div className="color-palette-details">
                  <div className={`color-palette-title color-tone-${index % 4}`}>
                    <h3>{color.nombre}</h3>
                    <span aria-label="Color disponible" />
                  </div>
                  <code>{color.codigoHex.toUpperCase()}</code>
                  <footer><span>{colorCode(color.id)}</span><strong>{color.productIds.size} {color.productIds.size === 1 ? 'producto' : 'productos'}</strong></footer>
                </div>
              </article>
            ))}
            {!loading && filteredPalette.length === 0 && (
              <p className="color-empty">{palette.length ? 'No hay colores que coincidan con la búsqueda.' : 'Aún no hay colores registrados.'}</p>
            )}
            {loading && <p className="color-empty">Consultando paleta…</p>}
          </div>
          <footer className="color-palette-footer">
            <span>Mostrando {filteredPalette.length} colores</span>
            <span>{palette.length} disponibles para asignar</span>
          </footer>
        </section>
      </div>
    </section>
  );
}

export default ColorsPanel;

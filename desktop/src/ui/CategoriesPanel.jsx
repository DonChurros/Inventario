import { useEffect, useMemo, useState } from 'react';

const initialForm = { nombre: '', descripcion: '', estado: 'Activo' };
const fallbackImageUrl = 'https://inventario.local/categorias/default.png';

function categoryCode(id) {
  return `CAT-${String(id).padStart(3, '0')}`;
}

function slugify(value) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
    .slice(0, 250);
}

function CategoriesPanel() {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [updatingCategoryIds, setUpdatingCategoryIds] = useState([]);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);
  const [categoryNotice, setCategoryNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [categoryList, productList] = await Promise.all([
        window.inventario.obtenerRegistros('categorias'),
        window.inventario.obtenerRegistros('productos'),
      ]);
      setCategories(categoryList);
      setProducts(productList);
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar las categorías.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const activeCount = categories.filter((category) => category.estado === 'Activo').length;
  const filteredCategories = useMemo(() => {
    const query = search.trim().toLocaleLowerCase();
    return categories.filter((category) => (
      !query || [category.nombre, category.slug, categoryCode(category.id)]
        .some((value) => value.toLocaleLowerCase().includes(query))
    ));
  }, [categories, search]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const nombre = form.nombre.trim();
    if (!nombre) {
      setNotice({ type: 'error', text: 'El nombre de la categoría es obligatorio.' });
      return;
    }
    setSaving(true);
    try {
      const created = await window.inventario.crearRegistro('categorias', {
        nombre,
        slug: slugify(nombre),
        descripcion: form.descripcion.trim() || null,
        imagenUrl: fallbackImageUrl,
        estado: form.estado,
      });
      setCategories((current) => [...current, created].sort((a, b) => a.nombre.localeCompare(b.nombre)));
      setForm(initialForm);
      setNotice({ type: 'success', text: 'Categoría creada correctamente.' });
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo crear la categoría.' });
    } finally {
      setSaving(false);
    }
  }

  async function toggleCategoryStatus(category) {
    const nextStatus = category.estado === 'Activo' ? 'Inactivo' : 'Activo';
    setUpdatingCategoryIds((current) => [...current, category.id]);
    setCategoryNotice(null);
    try {
      const updated = await window.inventario.actualizarEstadoCategoria(category.id, nextStatus);
      setCategories((current) => current.map((item) => (
        item.id === updated.id ? updated : item
      )));
    } catch (updateError) {
      setCategoryNotice({
        id: category.id,
        text: updateError.message || `No se pudo actualizar ${category.nombre}.`,
      });
    } finally {
      setUpdatingCategoryIds((current) => current.filter((id) => id !== category.id));
    }
  }

  return (
    <section className="categories-page" aria-labelledby="categories-title">
      <header className="categories-heading">
        <div>
          <span className="eyebrow">CARACTERÍSTICAS DE PRODUCTO</span>
          <h1 id="categories-title">Crear categoría</h1>
          <p>Organiza el catálogo y amplía la oferta más allá de las pijamas.</p>
        </div>
        <div className="category-summary">
          <div><span>Categorías</span><strong>{categories.length}</strong></div>
          <div className="active"><span>Activas</span><strong>{activeCount}</strong></div>
        </div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="categories-grid">
        <section className="category-card category-form-card">
          <header className="category-card-heading">
            <span className="category-heading-icon" aria-hidden="true">◇</span>
            <div><h2>Nueva categoría</h2><p>Define cómo se agruparán los productos</p></div>
          </header>
          <form className="category-form" onSubmit={submit}>
            <div className="category-field-row">
              <label className="field">
                <span>ID de categoría</span>
                <input value="Asignado automáticamente" readOnly aria-describedby="category-id-hint" />
              </label>
              <label className="field">
                <span>Nombre</span>
                <input name="nombre" maxLength="100" placeholder="Ej. Ropa deportiva" value={form.nombre} onChange={updateField} required />
              </label>
            </div>
            <span className="category-id-hint" id="category-id-hint">El código CAT se genera al crear el registro.</span>
            <label className="field">
              <span>Descripción</span>
              <textarea name="descripcion" rows="4" maxLength="2000" placeholder="Describe qué productos pertenecen a esta categoría…" value={form.descripcion} onChange={updateField} />
            </label>
            <label className="field">
              <span>Estado inicial</span>
              <select name="estado" value={form.estado} onChange={updateField} required>
                <option value="Activo">Activa</option>
                <option value="Inactivo">Inactiva</option>
              </select>
            </label>
            <p className="category-info">Las categorías activas aparecerán como opción al registrar o editar productos.</p>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="category-submit" type="submit" disabled={saving || loading || !form.nombre.trim()}>
              {saving ? 'Creando…' : '＋　Crear categoría'}
            </button>
          </form>
        </section>

        <section className="category-card category-list-card">
          <header className="category-list-heading">
            <div><h2>Categorías del catálogo</h2><p>{products.length} productos organizados actualmente</p></div>
            <input type="search" aria-label="Buscar categoría" placeholder="Buscar categoría…" value={search} onChange={(event) => setSearch(event.target.value)} />
          </header>
          <div className="category-list">
            {filteredCategories.map((category, index) => {
              const productCount = products.filter((product) => Number(product.categoriaId) === Number(category.id)).length;
              return (
                <article className="category-item" key={category.id}>
                  <div className={`category-item-icon category-color-${index % 4}`} aria-hidden="true">◇</div>
                  <span className={`category-status ${category.estado === 'Activo' ? 'is-active' : ''}`}>
                    <i aria-hidden="true" />{category.estado === 'Activo' ? 'Activa' : 'Inactiva'}
                  </span>
                  <button
                    className={`category-switch ${category.estado === 'Activo' ? 'is-on' : ''}`}
                    type="button"
                    role="switch"
                    aria-label={`${category.estado === 'Activo' ? 'Desactivar' : 'Activar'} categoría ${category.nombre}`}
                    aria-checked={category.estado === 'Activo'}
                    disabled={updatingCategoryIds.includes(category.id)}
                    onClick={() => void toggleCategoryStatus(category)}
                  >
                    <span />
                  </button>
                  <div className="category-item-title"><h3>{category.nombre}</h3><small>{categoryCode(category.id)}</small></div>
                  <p>{category.descripcion || 'Sin descripción.'}</p>
                  <footer><span>Productos asignados</span><strong>{productCount}</strong></footer>
                  {categoryNotice?.id === category.id && <p className="category-operation-error" role="alert">{categoryNotice.text}</p>}
                </article>
              );
            })}
            {!loading && filteredCategories.length === 0 && (
              <p className="category-empty">{categories.length ? 'No hay categorías que coincidan con la búsqueda.' : 'Aún no hay categorías registradas.'}</p>
            )}
            {loading && <p className="category-empty">Consultando categorías…</p>}
          </div>
          <footer className="category-list-footer">Mostrando {filteredCategories.length} de {categories.length} categorías</footer>
        </section>
      </div>
    </section>
  );
}

export default CategoriesPanel;

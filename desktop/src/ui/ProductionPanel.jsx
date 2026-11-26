import { useEffect, useMemo, useState } from 'react';

const money = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 2,
});

function ProductionPanel() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [clearingProductId, setClearingProductId] = useState(null);
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    try {
      const [productList, options] = await Promise.all([
        window.inventario.listarProductos(),
        window.inventario.obtenerOpciones(),
      ]);
      setProducts(productList);
      setCategories(options.categorias);
      setProductId((current) => (
        productList.some((product) => String(product.id) === current)
          ? current
          : String(productList[0]?.id || '')
      ));
      setNotice(null);
    } catch (error) {
      setNotice({ type: 'error', text: error.message || 'No se pudieron cargar los productos.' });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const selectedProduct = useMemo(
    () => products.find((product) => String(product.id) === productId),
    [products, productId],
  );
  const category = categories.find((item) => item.id === selectedProduct?.categoriaId);

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const cantidad = Number(quantity);
    if (!selectedProduct || !Number.isInteger(cantidad) || cantidad <= 0) {
      setNotice({ type: 'error', text: 'Selecciona un producto e ingresa una cantidad entera mayor que cero.' });
      return;
    }

    setSaving(true);
    try {
      const ingreso = await window.inventario.crearRegistro('produccion', {
        productoId: selectedProduct.id,
        cantidad,
      });
      setProducts((current) => current.map((product) => (
        product.id === ingreso.productoId
          ? { ...product, existencias: ingreso.existenciasActuales }
          : product
      )));
      setQuantity('1');
      setNotice({
        type: 'success',
        text: `Ingreso registrado. Existencias actuales: ${ingreso.existenciasActuales}.`,
      });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || 'No se pudo registrar el ingreso.' });
    } finally {
      setSaving(false);
    }
  }

  async function clearStock(product) {
    if (product.existencias <= 0 || !window.confirm(
      `¿Eliminar las ${product.existencias} unidades de ${product.nombre} y dejar sus existencias en cero?`,
    )) {
      return;
    }
    setNotice(null);
    setClearingProductId(product.id);
    try {
      const result = await window.inventario.eliminarExistenciasProducto(product.id);
      setProducts((current) => current.map((product) => (
        product.id === result.productoId
          ? { ...product, existencias: result.existencias }
          : product
      )));
      setNotice({
        type: 'success',
        text: `Existencias de ${product.nombre} eliminadas. El producto sigue en el catálogo.`,
      });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || 'No se pudieron eliminar las existencias.' });
    } finally {
      setClearingProductId(null);
    }
  }

  return (
    <section className="production-page">
      <header className="production-heading">
        <div>
          <span className="eyebrow">INVENTARIO · PRODUCCIÓN</span>
          <h1>Ingreso de productos</h1>
          <p>Registra unidades producidas y súmalas a las existencias del producto.</p>
        </div>
        <button className="refresh-button" type="button" onClick={() => void load()} disabled={loading}>
          {loading ? 'Cargando…' : 'Actualizar productos'}
        </button>
      </header>

      <div className="production-grid">
        <section className="management-card">
          <span className="eyebrow">NUEVO INGRESO</span>
          <h2>Datos de producción</h2>
          <form className="management-form" onSubmit={submit}>
            <label className="field">
              <span>Producto</span>
              <select value={productId} onChange={(event) => setProductId(event.target.value)} required disabled={loading}>
                <option value="">Selecciona un producto</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>#{product.id} · {product.nombre}</option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>Categoría</span>
              <input value={category?.nombre || '—'} readOnly />
            </label>
            <label className="field">
              <span>Precio unitario</span>
              <input value={selectedProduct ? money.format(Number(selectedProduct.costo)) : '—'} readOnly />
            </label>
            <label className="field">
              <span>Cantidad producida</span>
              <input
                type="number"
                min="1"
                step="1"
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                required
              />
            </label>
            {selectedProduct && (
              <p className="production-stock">
                Existencias actuales: <strong>{selectedProduct.existencias}</strong>
              </p>
            )}
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button
              type="submit"
              disabled={saving || loading || clearingProductId !== null || !selectedProduct}
            >
              {saving ? 'Registrando…' : 'Registrar ingreso'}
            </button>
          </form>
        </section>

        <section className="management-card">
          <span className="eyebrow">CATÁLOGO</span>
          <h2>Existencias por producto</h2>
          {loading ? <p>Consultando productos…</p> : products.length === 0 ? (
            <p>No hay productos registrados para ingresar a producción.</p>
          ) : (
            <div className="management-list">
              {products.map((product) => (
                <div className="management-record" key={product.id}>
                  <strong>#{product.id} · {product.nombre}</strong>
                  <span>{categories.find((item) => item.id === product.categoriaId)?.nombre || 'Categoría'} · {product.existencias} unidades · {money.format(Number(product.costo))}</span>
                  {product.existencias > 0 ? (
                    <button
                      className="production-stock-action"
                      type="button"
                      onClick={() => void clearStock(product)}
                      disabled={saving || clearingProductId !== null}
                    >
                      {clearingProductId === product.id ? 'Eliminando…' : 'Eliminar stock'}
                    </button>
                  ) : <span>Sin existencias</span>}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </section>
  );
}

export default ProductionPanel;

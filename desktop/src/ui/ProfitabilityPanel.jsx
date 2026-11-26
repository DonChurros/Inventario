import { useEffect, useState } from 'react';

const money = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 2,
});

function ProfitabilityPanel() {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadReport() {
    setLoading(true);
    setError('');
    try {
      setReport(await window.inventario.obtenerRentabilidad());
    } catch (loadError) {
      setError(loadError.message || 'No se pudo cargar el resumen de ventas.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadReport();
  }, []);

  return (
    <section className="profitability-page" aria-labelledby="profitability-title">
      <header className="profitability-heading">
        <div>
          <span className="eyebrow">HISTORIA DE USUARIO · ACCESO EXCLUSIVO DEL JEFE</span>
          <h1 id="profitability-title">Rentabilidad y ventas</h1>
          <p>Consulta los ingresos registrados y los productos con mayor movimiento para orientar la producción.</p>
        </div>
        <button className="refresh-button" type="button" onClick={() => void loadReport()} disabled={loading}>
          {loading ? 'Actualizando…' : 'Actualizar'}
        </button>
      </header>

      <div className="profitability-note" role="note">
        Este reporte muestra ventas brutas registradas, no utilidad neta. No descuenta insumos ni mano de obra e incluye las ventas pendientes.
      </div>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="profitability-stats">
        <article className="profitability-stat">
          <span>Ventas con productos</span>
          <strong>{loading ? '—' : report?.ventasConProductos ?? 0}</strong>
          <small>Acumulado histórico</small>
        </article>
        <article className="profitability-stat">
          <span>Unidades registradas</span>
          <strong>{loading ? '—' : report?.unidadesVendidas ?? 0}</strong>
          <small>En todas las ventas</small>
        </article>
        <article className="profitability-stat highlight">
          <span>Ventas brutas · sin IVA</span>
          <strong>{loading ? '—' : money.format(Number(report?.ingresosBrutos ?? 0))}</strong>
          <small>Precio unitario × cantidad</small>
        </article>
      </div>

      <section className="profitability-card" aria-labelledby="profitability-products-title">
        <div className="profitability-card-heading">
          <div>
            <h2 id="profitability-products-title">Productos más vendidos</h2>
            <p>Ordenados por el valor bruto registrado en sus ventas.</p>
          </div>
        </div>
        <div className="profitability-table-scroll">
          <table className="profitability-table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Unidades vendidas</th>
                <th>Ventas brutas · sin IVA</th>
                <th>Existencias actuales</th>
              </tr>
            </thead>
            <tbody>
              {report?.productos.map((product) => (
                <tr key={product.productoId}>
                  <td><strong>{product.nombre}</strong><small>#{product.productoId}</small></td>
                  <td>{product.unidadesVendidas}</td>
                  <td>{money.format(Number(product.ingresosBrutos))}</td>
                  <td>{product.existenciasActuales}</td>
                </tr>
              ))}
              {!loading && report?.productos.length === 0 && (
                <tr><td className="profitability-empty" colSpan="4">Aún no hay ventas con productos registrados.</td></tr>
              )}
              {loading && (
                <tr><td className="profitability-empty" colSpan="4">Consultando las ventas…</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

export default ProfitabilityPanel;

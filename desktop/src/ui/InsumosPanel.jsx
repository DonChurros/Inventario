import { useEffect, useMemo, useState } from 'react';

const measurementUnits = [
  'Carrete',
  'Cono',
  'Unidad',
  'Metro',
  'Paquete',
  'Rollo',
  'Par',
  'Caja',
  'Bolsa',
  'Docena',
  'Gramo',
  'Kilogramo',
  'Litro',
];

const initialForm = {
  nombre: '',
  referencia: '',
  unidadMedida: '',
  costoUnitario: '',
  cantidadDisponible: '',
};

const money = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 2,
});

function InsumosPanel() {
  const [form, setForm] = useState(initialForm);
  const [supplies, setSupplies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      setSupplies(await window.inventario.obtenerRegistros('insumos'));
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar los insumos.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const totalInventoryValue = useMemo(() => supplies.reduce(
    (total, supply) => total + Number(supply.costoUnitario) * Number(supply.cantidadDisponible),
    0,
  ), [supplies]);
  const lowStockCount = supplies.filter((supply) => Number(supply.cantidadDisponible) <= 10).length;
  const initialValue = Number(form.costoUnitario || 0) * Number(form.cantidadDisponible || 0);
  const canSubmit = form.nombre.trim() && form.referencia.trim() && form.unidadMedida.trim()
    && measurementUnits.includes(form.unidadMedida)
    && form.costoUnitario !== '' && form.cantidadDisponible !== ''
    && Number.isFinite(Number(form.costoUnitario)) && Number(form.costoUnitario) >= 0
    && Number.isFinite(Number(form.cantidadDisponible)) && Number(form.cantidadDisponible) >= 0;

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const payload = {
      nombre: form.nombre.trim(),
      referencia: form.referencia.trim(),
      unidadMedida: form.unidadMedida,
      costoUnitario: Number(form.costoUnitario),
      cantidadDisponible: Number(form.cantidadDisponible),
    };
    if (!canSubmit) {
      setNotice({ type: 'error', text: 'Completa todos los campos con valores válidos y no negativos.' });
      return;
    }
    if (!/^\d+(\.\d{1,2})?$/.test(form.costoUnitario)
      || !/^\d+(\.\d{1,2})?$/.test(form.cantidadDisponible)) {
      setNotice({ type: 'error', text: 'El costo y la cantidad admiten máximo dos decimales.' });
      return;
    }

    setSaving(true);
    try {
      const created = await window.inventario.crearRegistro('insumos', payload);
      setSupplies((current) => [...current, created].sort((a, b) => a.nombre.localeCompare(b.nombre)));
      setForm(initialForm);
      setNotice({ type: 'success', text: 'Insumo registrado correctamente.' });
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo registrar el insumo.' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="supplies-page" aria-labelledby="supplies-title">
      <header className="supplies-heading">
        <div>
          <span className="eyebrow">MATERIALES Y COSTEO</span>
          <h1 id="supplies-title">Registrar insumo menor</h1>
          <p>Controla los materiales complementarios utilizados en producción.</p>
        </div>
        <div className="supply-summary">
          <div className="supply-count"><span>Referencias</span><strong>{supplies.length}</strong></div>
          <div className="supply-count low-stock-summary"><span>Stock bajo</span><strong>{lowStockCount}</strong></div>
        </div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="supplies-grid">
        <section className="supplies-card">
          <div className="supplies-card-heading">
            <span className="supplies-icon" aria-hidden="true">＋</span>
            <div><h2>Información del insumo</h2><p>Todos los campos son obligatorios</p></div>
          </div>
          <form className="supply-form" onSubmit={submit}>
            <label className="field">
              <span>Nombre del insumo</span>
              <input name="nombre" maxLength="100" placeholder="Ej. Hilo poliéster negro" value={form.nombre} onChange={updateField} required />
            </label>
            <div className="supply-field-row">
              <label className="field">
                <span>Referencia</span>
                <input name="referencia" maxLength="50" placeholder="Ej. HIL-002" value={form.referencia} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Unidad de medida</span>
                <select name="unidadMedida" value={form.unidadMedida} onChange={updateField} required>
                  <option value="">Seleccionar</option>
                  {measurementUnits.map((unit) => <option key={unit} value={unit}>{unit}</option>)}
                </select>
              </label>
            </div>
            <div className="supply-field-row">
              <label className="field">
                <span>Costo unitario</span>
                <input name="costoUnitario" type="number" min="0" step="0.01" placeholder="0.00" value={form.costoUnitario} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Cantidad disponible</span>
                <input name="cantidadDisponible" type="number" min="0" step="0.01" placeholder="0.00" value={form.cantidadDisponible} onChange={updateField} required />
              </label>
            </div>
            <div className="supply-initial-value">
              <span>Valor inicial del insumo</span>
              <strong>{money.format(initialValue)}</strong>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="supply-submit" type="submit" disabled={saving || loading || !canSubmit}>
              {saving ? 'Registrando…' : '＋　Registrar insumo'}
            </button>
            <p className="supply-form-hint">{canSubmit ? 'La referencia no puede estar duplicada.' : 'Completa los campos resaltados para continuar.'}</p>
          </form>
        </section>

        <section className="supplies-card supplies-list-card">
          <div className="supplies-list-heading">
            <div><h2>Inventario de insumos</h2><p>Existencias y costos actuales</p></div>
            <div className="supply-inventory-value"><span>VALOR INVENTARIO</span><strong>{money.format(totalInventoryValue)}</strong></div>
            <button className="refresh-button" type="button" onClick={() => void load()} disabled={loading}>{loading ? 'Cargando…' : 'Actualizar'}</button>
          </div>
          <div className="supply-table-scroll">
            <table className="supply-table">
              <thead><tr><th>Insumo</th><th>Unidad</th><th>Costo</th><th>Disponible</th><th>Subtotal</th></tr></thead>
              <tbody>
                {supplies.map((supply) => (
                  <tr key={supply.id}>
                    <td><span className="supply-entry"><span className="supply-avatar">{supply.nombre.slice(0, 2).toUpperCase()}</span><span className="supply-name"><strong>{supply.nombre}</strong><small>{supply.referencia}</small></span></span></td>
                    <td>{supply.unidadMedida}</td>
                    <td>{money.format(Number(supply.costoUnitario))}</td>
                    <td><span className={`supply-quantity ${Number(supply.cantidadDisponible) <= 10 ? 'low' : ''}`}>{Number(supply.cantidadDisponible).toLocaleString('es-CO')}</span></td>
                    <td className="supply-subtotal">{money.format(Number(supply.costoUnitario) * Number(supply.cantidadDisponible))}</td>
                  </tr>
                ))}
                {!loading && supplies.length === 0 && <tr><td className="supply-empty" colSpan="5">Aún no hay insumos registrados.</td></tr>}
              </tbody>
            </table>
          </div>
          <p className="supply-list-count"><span>{supplies.length} insumos registrados</span>{lowStockCount > 0 && <strong>{lowStockCount} con stock bajo</strong>}</p>
        </section>
      </div>
    </section>
  );
}

export default InsumosPanel;

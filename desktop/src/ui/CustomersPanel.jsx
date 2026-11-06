import { useEffect, useMemo, useState } from 'react';

const initialForm = {
  id: '',
  documentoId: '',
  ciudadId: '',
  nombre: '',
  direccion: '',
  telefono: '',
  correo: '',
  fechaNacimiento: '',
  contrasena: '',
  confirmarContrasena: '',
};

function CustomersPanel() {
  const [form, setForm] = useState(initialForm);
  const [customers, setCustomers] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [customerList, catalogs] = await Promise.all([
        window.inventario.obtenerRegistros('usuariosCompletos'),
        window.inventario.obtenerRegistros('catalogosUsuario'),
      ]);
      setCustomers(customerList);
      setDocuments(catalogs.documentos);
      setCities(catalogs.ciudades);
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar los clientes y catálogos.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const requiredValues = [
    form.id,
    form.documentoId,
    form.ciudadId,
    form.nombre.trim(),
    form.direccion.trim(),
    form.telefono.trim(),
    form.correo.trim(),
    form.fechaNacimiento,
    form.contrasena,
    form.confirmarContrasena,
  ];
  const complete = requiredValues.every(Boolean);
  const passwordsMatch = form.contrasena.length >= 8
    && form.contrasena === form.confirmarContrasena;
  const selectedDocument = documents.find((document) => String(document.id) === form.documentoId);
  const selectedCity = cities.find((city) => String(city.id) === form.ciudadId);
  const recentCustomers = useMemo(() => customers.slice(0, 5), [customers]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const id = Number(form.id);
    const payload = {
      id,
      documentoId: Number(form.documentoId),
      ciudadId: Number(form.ciudadId),
      nombre: form.nombre.trim(),
      direccion: form.direccion.trim(),
      telefono: form.telefono.trim(),
      correo: form.correo.trim(),
      fechaNacimiento: form.fechaNacimiento,
      contrasena: form.contrasena,
      confirmarContrasena: form.confirmarContrasena,
    };

    if (!complete) {
      setNotice({ type: 'error', text: 'Completa todos los campos obligatorios.' });
      return;
    }
    if (!Number.isInteger(id) || id < 100000 || id > 2147483647) {
      setNotice({ type: 'error', text: 'La identificación debe tener de 6 a 10 dígitos y estar dentro del rango permitido.' });
      return;
    }
    if (!passwordsMatch) {
      setNotice({ type: 'error', text: 'La contraseña debe tener al menos 8 caracteres y ambas deben coincidir.' });
      return;
    }

    setSaving(true);
    try {
      await window.inventario.crearRegistro('usuarios', payload);
      setForm(initialForm);
      setNotice({ type: 'success', text: 'Cliente y acceso registrados correctamente.' });
      await load();
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo registrar el cliente.' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="customers-page" aria-labelledby="customers-title">
      <header className="customers-heading">
        <div>
          <span className="eyebrow">ADMINISTRACIÓN DE ACCESO</span>
          <h1 id="customers-title">Crear usuario</h1>
          <p>Registra los datos del cliente y crea su acceso con una contraseña segura.</p>
        </div>
        <div className="protected-badge"><span aria-hidden="true">♢</span> Registro protegido</div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="customers-grid">
        <section className="customers-card">
          <div className="customers-card-heading">
            <span className="customers-card-icon" aria-hidden="true">♢</span>
            <div><h2>Datos del cliente y acceso</h2><p>El perfil se guarda en Usuarios; el hash en credenciales separadas.</p></div>
          </div>
          <form className="customer-form" onSubmit={submit}>
            <label className="field">
              <span>Número de identificación</span>
              <input
                name="id"
                inputMode="numeric"
                pattern="[0-9]{6,10}"
                maxLength="10"
                placeholder="Entre 6 y 10 dígitos"
                value={form.id}
                onChange={updateField}
                required
              />
            </label>
            <div className="customer-field-row">
              <label className="field">
                <span>Tipo de documento</span>
                <select name="documentoId" value={form.documentoId} onChange={updateField} required>
                  <option value="">Seleccionar</option>
                  {documents.map((document) => <option key={document.id} value={document.id}>{document.nombre}</option>)}
                </select>
              </label>
              <label className="field">
                <span>Ciudad</span>
                <select name="ciudadId" value={form.ciudadId} onChange={updateField} required>
                  <option value="">Seleccionar</option>
                  {cities.map((city) => <option key={city.id} value={city.id}>{city.nombre}</option>)}
                </select>
              </label>
            </div>
            <label className="field">
              <span>Nombre completo</span>
              <input name="nombre" maxLength="100" placeholder="Ej. María Castillo" value={form.nombre} onChange={updateField} required />
            </label>
            <label className="field">
              <span>Dirección</span>
              <input name="direccion" maxLength="150" placeholder="Dirección de residencia" value={form.direccion} onChange={updateField} required />
            </label>
            <div className="customer-field-row">
              <label className="field">
                <span>Teléfono</span>
                <input name="telefono" maxLength="20" placeholder="Número de contacto" value={form.telefono} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Fecha de nacimiento</span>
                <input name="fechaNacimiento" type="date" max={new Date().toISOString().slice(0, 10)} value={form.fechaNacimiento} onChange={updateField} required />
              </label>
            </div>
            <label className="field">
              <span>Correo electrónico</span>
              <input name="correo" type="email" maxLength="100" placeholder="nombre@correo.com" value={form.correo} onChange={updateField} required />
            </label>
            <div className="customer-field-row">
              <label className="field">
                <span>Contraseña</span>
                <input name="contrasena" type="password" minLength="8" autoComplete="new-password" placeholder="Mín. 8 caracteres" value={form.contrasena} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Confirmar contraseña</span>
                <input name="confirmarContrasena" type="password" minLength="8" autoComplete="new-password" placeholder="Repite la contraseña" value={form.confirmarContrasena} onChange={updateField} required />
              </label>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="customer-submit" type="submit" disabled={saving || loading || !complete || !passwordsMatch}>
              {saving ? 'Creando…' : '＋　Crear usuario'}
            </button>
            <p className="customer-form-hint">
              {!complete ? 'Completa todos los campos obligatorios' : !passwordsMatch ? 'Verifica que las contraseñas coincidan' : 'La contraseña se guarda protegida'}
            </p>
          </form>
        </section>

        <div className="customers-side-column">
          <section className="customers-card identity-card">
            <div className="identity-heading">
              <div><h2>Verificación de identidad</h2><p>Revisa los datos del cliente antes de crear su acceso.</p></div>
              <span className={`identity-state ${complete ? 'ready' : ''}`}>{complete ? 'Completo' : 'Pendiente'}</span>
            </div>
            <div className="identity-preview">
              <span className="identity-icon" aria-hidden="true">♧</span>
              {form.id || form.correo ? (
                <div>
                  <strong>{form.nombre || 'Nuevo cliente'}</strong>
                  <span>{selectedDocument?.nombre || 'Tipo de documento'} · {form.id || 'Identificación'}</span>
                  <span>{form.correo || 'Correo electrónico'}</span>
                  <span>{selectedCity?.nombre || 'Ciudad'}</span>
                </div>
              ) : <p>Completa los datos para revisar el registro antes de guardarlo.</p>}
            </div>
          </section>

          <section className="customers-card recent-customers-card">
            <div className="recent-customers-heading">
              <div><h2>Clientes recientes</h2><p>Perfiles registrados en la base de datos</p></div>
              <button className="refresh-button" type="button" onClick={() => void load()} disabled={loading}>
                {loading ? 'Cargando…' : 'Actualizar'}
              </button>
            </div>
            <div className="recent-customers-list">
              {recentCustomers.map((customer) => (
                <div className="recent-customer" key={customer.id}>
                  <span className="customer-avatar">{customer.nombre.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span>
                  <span className="customer-identity"><strong>{customer.nombre}</strong><small>{customer.correo}</small></span>
                  <time>{customer.fechaRegistro ? new Date(customer.fechaRegistro).toLocaleDateString('es-CO') : ''}</time>
                </div>
              ))}
              {!loading && recentCustomers.length === 0 && <p className="customer-empty">Aún no hay clientes registrados.</p>}
            </div>
          </section>
        </div>
      </div>
    </section>
  );
}

export default CustomersPanel;

import { useEffect, useMemo, useState } from 'react';

const initialForm = {
  id: '',
  documentoId: '',
  estado: '',
  nombre: '',
  telefono: '',
  correo: '',
  contrasena: '',
  rolId: '',
};

function EmployeesPanel() {
  const [form, setForm] = useState(initialForm);
  const [employees, setEmployees] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [roles, setRoles] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [employeeList, documentOptions, roleOptions] = await Promise.all([
        window.inventario.obtenerRegistros('empleados'),
        window.inventario.obtenerRegistros('documentos'),
        window.inventario.obtenerRegistros('roles'),
      ]);
      setEmployees(employeeList);
      setDocuments(documentOptions);
      setRoles(roleOptions);
    } catch (loadError) {
      setError(loadError.message || 'No se pudieron cargar los empleados y sus catálogos.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const activeCount = employees.filter((employee) => employee.estado === 'Activo').length;
  const filteredEmployees = useMemo(() => {
    const query = search.trim().toLocaleLowerCase();
    if (!query) return employees;
    return employees.filter((employee) => (
      [employee.nombre, employee.correo, employee.rol, String(employee.id)]
        .some((value) => value?.toLocaleLowerCase().includes(query))
    ));
  }, [employees, search]);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setNotice(null);
  }

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    const payload = {
      id: Number(form.id),
      documentoId: Number(form.documentoId),
      estado: form.estado,
      nombre: form.nombre.trim(),
      telefono: form.telefono.trim() || null,
      correo: form.correo.trim(),
      contrasena: form.contrasena,
      rolId: Number(form.rolId),
    };
    if (!Number.isInteger(payload.id) || payload.id <= 0 || !payload.documentoId
      || !payload.estado || !payload.nombre || !payload.correo || !payload.contrasena
      || !payload.rolId) {
      setNotice({ type: 'error', text: 'Completa todos los campos obligatorios para registrar al empleado.' });
      return;
    }
    if (payload.contrasena.length < 8) {
      setNotice({ type: 'error', text: 'La contraseña temporal debe tener al menos 8 caracteres.' });
      return;
    }

    setSaving(true);
    try {
      await window.inventario.crearRegistro('empleados', payload);
      setForm(initialForm);
      setNotice({ type: 'success', text: 'Empleado registrado correctamente.' });
      await load();
    } catch (saveError) {
      setNotice({ type: 'error', text: saveError.message || 'No se pudo registrar el empleado.' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="employees-page" aria-labelledby="employees-title">
      <header className="employees-heading">
        <div>
          <span className="eyebrow">ADMINISTRACIÓN DE EMPLEADOS</span>
          <h1 id="employees-title">Registrar empleado</h1>
          <p>Crea accesos individuales y asigna responsabilidades dentro del sistema.</p>
        </div>
        <div className="employee-stats">
          <div className="employee-stat"><span>Empleados</span><strong>{employees.length}</strong></div>
          <div className="employee-stat active"><span>Activos</span><strong>{activeCount}</strong></div>
        </div>
      </header>

      {error && <p className="products-error" role="alert">{error}</p>}

      <div className="employees-grid">
        <section className="employees-card">
          <div className="employees-card-heading">
            <span className="employees-card-icon" aria-hidden="true">♧</span>
            <div><h2>Datos del empleado</h2><p>Todos los campos son obligatorios excepto el teléfono</p></div>
          </div>
          <form className="employee-form" onSubmit={submit}>
            <div className="employee-form-grid">
              <label className="field">
                <span>ID del empleado</span>
                <input name="id" type="number" min="1" step="1" placeholder="Ej. 1005" value={form.id} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Estado</span>
                <select name="estado" value={form.estado} onChange={updateField} required>
                  <option value="">Seleccionar</option>
                  <option value="Activo">Activo</option>
                  <option value="Inactivo">Inactivo</option>
                </select>
              </label>
              <label className="field employee-field-wide">
                <span>Tipo de documento</span>
                <select name="documentoId" value={form.documentoId} onChange={updateField} required>
                  <option value="">Seleccionar tipo de documento</option>
                  {documents.map((document) => <option key={document.id} value={document.id}>{document.nombre}</option>)}
                </select>
              </label>
              <label className="field employee-field-wide">
                <span>Nombre completo</span>
                <input name="nombre" maxLength="100" placeholder="Ej. Sofía Martínez" value={form.nombre} onChange={updateField} required />
              </label>
              <label className="field employee-field-wide">
                <span>Correo corporativo</span>
                <input name="correo" type="email" maxLength="100" placeholder="nombre@empresa.com" value={form.correo} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Contraseña temporal</span>
                <input name="contrasena" type="password" minLength="8" autoComplete="new-password" placeholder="Mín. 8 caracteres" value={form.contrasena} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Rol en el sistema</span>
                <select name="rolId" value={form.rolId} onChange={updateField} required>
                  <option value="">Seleccionar</option>
                  {roles.map((role) => <option key={role.id} value={role.id}>{role.nombre}</option>)}
                </select>
              </label>
              <label className="field employee-field-wide">
                <span>Teléfono <span className="optional">· opcional</span></span>
                <input name="telefono" maxLength="20" placeholder="Número de contacto" value={form.telefono} onChange={updateField} />
              </label>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="employee-submit" type="submit" disabled={saving || loading || !roles.length || !documents.length}>
              {saving ? 'Registrando…' : '＋　Registrar empleado'}
            </button>
            {(!roles.length || !documents.length) && !loading && (
              <p className="employee-form-hint">Registra tipos de documento y roles en sus catálogos para habilitar el formulario.</p>
            )}
          </form>
        </section>

        <section className="employees-card employees-list-card">
          <div className="employees-list-heading">
            <div><h2>Equipo registrado</h2><p>Empleados con acceso al sistema</p></div>
            <div className="employee-list-actions">
              <input
                type="search"
                aria-label="Buscar empleado"
                placeholder="Buscar empleado…"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <button className="refresh-button" type="button" onClick={() => void load()} disabled={loading}>
                {loading ? 'Cargando…' : 'Actualizar'}
              </button>
            </div>
          </div>
          <div className="employee-table-scroll">
            <table className="employee-table">
              <thead><tr><th>Empleado</th><th>Rol</th><th>Estado</th><th>ID</th></tr></thead>
              <tbody>
                {filteredEmployees.map((employee) => (
                  <tr key={employee.id}>
                    <td><span className="employee-avatar">{employee.nombre.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span>
                      <span className="employee-identity"><strong>{employee.nombre}</strong><small>{employee.correo}</small></span>
                    </td>
                    <td><span className="employee-role">{employee.rol}</span></td>
                    <td><span className={`employee-status ${employee.estado.toLowerCase()}`}><i />{employee.estado}</span></td>
                    <td className="employee-id">{employee.id}</td>
                  </tr>
                ))}
                {!loading && filteredEmployees.length === 0 && (
                  <tr><td className="employee-empty" colSpan="4">{employees.length ? 'No hay empleados que coincidan con la búsqueda.' : 'Aún no hay empleados registrados.'}</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <p className="employee-list-count">Mostrando {filteredEmployees.length} de {employees.length} empleados</p>
        </section>
      </div>
    </section>
  );
}

export default EmployeesPanel;

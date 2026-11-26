import { useState } from 'react';

function PasswordPanel() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState(null);

  async function submit(event) {
    event.preventDefault();
    setNotice(null);
    if (newPassword.length < 8 || newPassword.length > 128) {
      setNotice({ type: 'error', text: 'La nueva contraseña debe tener entre 8 y 128 caracteres.' });
      return;
    }
    if (newPassword !== confirmPassword) {
      setNotice({ type: 'error', text: 'La confirmación no coincide con la nueva contraseña.' });
      return;
    }
    if (newPassword === currentPassword) {
      setNotice({ type: 'error', text: 'La nueva contraseña debe ser diferente a la actual.' });
      return;
    }

    setSaving(true);
    try {
      await window.inventario.cambiarMiContrasena({
        currentPassword,
        newPassword,
        confirmPassword,
      });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setNotice({
        type: 'success',
        text: 'Contraseña actualizada. Tu sesión sigue activa; usa la nueva contraseña la próxima vez que inicies sesión.',
      });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || 'No se pudo actualizar la contraseña.' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="employees-page password-settings-page" aria-labelledby="password-title">
      <header className="employees-heading">
        <div>
          <span className="eyebrow">SEGURIDAD DE LA CUENTA</span>
          <h1 id="password-title">Cambiar contraseña</h1>
          <p>Actualiza tu contraseña para mantener el acceso seguro a tu cuenta.</p>
        </div>
      </header>
      <div className="employees-grid password-settings-grid">
        <section className="employees-card" aria-labelledby="password-form-title">
          <div className="employees-card-heading">
            <span className="employees-card-icon" aria-hidden="true">⌑</span>
            <div>
              <h2 id="password-form-title">Verifica tu identidad</h2>
              <p>Necesitarás tu contraseña actual y la nueva contraseña.</p>
            </div>
          </div>
          <form className="employee-form" onSubmit={submit}>
            <div className="password-fields">
              <label className="field">
                <span>Contraseña actual</span>
                <input
                  type="password"
                  autoComplete="current-password"
                  value={currentPassword}
                  onChange={(event) => setCurrentPassword(event.target.value)}
                  required
                />
              </label>
              <label className="field">
                <span>Nueva contraseña</span>
                <input
                  type="password"
                  autoComplete="new-password"
                  minLength="8"
                  maxLength="128"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  required
                />
              </label>
              <label className="field">
                <span>Confirmar nueva contraseña</span>
                <input
                  type="password"
                  autoComplete="new-password"
                  minLength="8"
                  maxLength="128"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  required
                />
              </label>
            </div>
            {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
            <button className="employee-submit" type="submit" disabled={saving}>
              {saving ? 'Actualizando…' : 'Actualizar contraseña'}
            </button>
            <p className="employee-form-hint">Debe tener entre 8 y 128 caracteres. Tu sesión actual no se cerrará.</p>
          </form>
        </section>
      </div>
    </section>
  );
}

export default PasswordPanel;

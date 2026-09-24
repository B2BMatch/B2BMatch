import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerService } from '../../services/authService';
import '../../styles/auth.css';

const AuthPanel = () => (
  <div className="auth-panel">
    <div className="auth-panel-brand">
      <span className="brand-mark">B2B</span>
      <span className="brand-name">B2BMatch</span>
    </div>
    <span className="badge-gold">Unite a la red</span>
    <h2>Creá tu cuenta y empezá hoy mismo.</h2>
    <p>Tanto si buscás talento como si ofrecés servicios, hay un lugar para vos.</p>
    <ul className="auth-features">
      <li><span className="auth-feature-icon">✓</span> Perfiles verificados para empresas y profesionales</li>
      <li><span className="auth-feature-icon">✓</span> Catálogo de servicios con cotización directa</li>
      <li><span className="auth-feature-icon">✓</span> Publicación de ofertas con seguimiento de postulantes</li>
    </ul>
  </div>
);

export const Register = ({ onSwitchToLogin, onSuccess }) => {
  const [userType, setUserType] = useState('user');
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const isEmbedded = typeof onSwitchToLogin === 'function';

  const roleMap = {
    user: 'PROFESSIONAL',
    company: 'COMPANY',
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await registerService({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        roleName: roleMap[userType],
      });

      const message = 'Registro exitoso. Ahora puedes iniciar sesión.';
      setSuccess(message);

      if (onSuccess) {
        onSuccess();
      } else {
        navigate('/login');
      }
    } catch (err) {
      console.error('Error al registrarse:', err);
      setError(
        err?.response?.data?.message || 'No se pudo completar el registro. Revisa los datos e inténtalo de nuevo.'
      );
    } finally {
      setLoading(false);
    }
  };

  const card = (
    <div className="auth-card">
      <div className="auth-card-head">
        <span className="badge-gold">Registro</span>
        <h2>Crear Cuenta</h2>
        <p>Únete a la plataforma B2BMatch</p>
      </div>

      <div className="type-toggle">
        <button
          type="button"
          className={userType === 'user' ? 'active' : ''}
          onClick={() => setUserType('user')}
        >
          👨‍💻 Postulante
        </button>
        <button
          type="button"
          className={userType === 'company' ? 'active' : ''}
          onClick={() => setUserType('company')}
        >
          🏢 Empresa
        </button>
      </div>

      {error && <div className="alert-banner alert-banner--error">{error}</div>}
      {success && <div className="alert-banner alert-banner--success">{success}</div>}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">{userType === 'user' ? 'Nombre Completo' : 'Nombre de la Empresa'}</label>
          <input
            type="text"
            name="name"
            className="form-input"
            value={formData.name}
            onChange={handleChange}
            placeholder={userType === 'user' ? 'Juan Pérez' : 'Acme B2B Corp'}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Correo Electrónico</label>
          <input
            type="email"
            name="email"
            className="form-input"
            value={formData.email}
            onChange={handleChange}
            placeholder="correo@ejemplo.com"
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Contraseña</label>
          <input
            type="password"
            name="password"
            className="form-input"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" className="btn-b2b-primary auth-submit" disabled={loading}>
          {loading ? 'Registrando...' : 'Registrarme'}
        </button>
      </form>

      <p className="auth-switch">
        ¿Ya tienes cuenta?{' '}
        {onSwitchToLogin ? (
          <button type="button" onClick={onSwitchToLogin}>
            Inicia Sesión
          </button>
        ) : (
          <Link to="/login">Inicia Sesión</Link>
        )}
      </p>
    </div>
  );

  if (isEmbedded) {
    return card;
  }

  return (
    <div className="auth-page">
      <AuthPanel />
      {card}
    </div>
  );
};

export default Register;
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import '../../styles/auth.css';

const AuthPanel = () => (
  <div className="auth-panel">
    <div className="auth-panel-brand">
      <span className="brand-mark">B2B</span>
      <span className="brand-name">B2BMatch</span>
    </div>
    <span className="badge-gold">Red de negocios</span>
    <h2>Conectá tu talento con oportunidades reales.</h2>
    <p>Un ecosistema donde profesionales y empresas se encuentran, colaboran y crecen.</p>
    <ul className="auth-features">
      <li><span className="auth-feature-icon">✓</span> Empresas verificadas que publican ofertas confiables</li>
      <li><span className="auth-feature-icon">✓</span> Postulación directa a las vacantes que te interesan</li>
      <li><span className="auth-feature-icon">✓</span> Reseñas de clientes que construyen tu reputación</li>
    </ul>
  </div>
);

export const Login = ({ onSwitchToRegister, onSuccess }) => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const isEmbedded = typeof onSwitchToRegister === 'function';

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const normalized = await loginUser(credentials);
      if (onSuccess) {
        onSuccess();
      } else if (normalized?.role === 'admin') {
        navigate('/admin');
      } else if (normalized?.role === 'company') {
        navigate('/empresa/ofertas');
      } else {
        navigate('/empleos');
      }
    } catch (err) {
      console.error('Error al iniciar sesión:', err);
      setError(err?.response?.data?.message || 'Credenciales inválidas o servidor no disponible.');
    } finally {
      setLoading(false);
    }
  };

  const card = (
    <div className="auth-card">
      <div className="auth-card-head">
        <span className="badge-gold">Cuenta</span>
        <h2>Iniciar Sesión</h2>
        <p>Ingresa a tu cuenta de B2BMatch</p>
      </div>

      {error && <div className="alert-banner alert-banner--error">{error}</div>}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Correo Electrónico</label>
          <input
            type="email"
            name="email"
            className="form-input"
            value={credentials.email}
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
            value={credentials.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" className="btn-b2b-primary auth-submit" disabled={loading}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>

      <p className="auth-switch">
        ¿No tienes cuenta?{' '}
        {onSwitchToRegister ? (
          <button type="button" onClick={onSwitchToRegister}>
            Regístrate
          </button>
        ) : (
          <Link to="/register">Regístrate</Link>
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

export default Login;
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const Login = ({ onSwitchToRegister, onSuccess }) => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { loginUser } = useAuth();
  const navigate = useNavigate();

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

  return (
    <div style={{ width: '100%', maxWidth: '400px', margin: '0 auto', padding: '10px' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '8px' }}>Iniciar Sesión</h2>
      <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: '24px', fontSize: '0.9rem' }}>
        Ingresa a tu cuenta de B2BMatch
      </p>

      {error && <p style={{ color: 'red', padding: '0 10px' }}>{error}</p>}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
            Correo Electrónico
          </label>
          <input
            type="email"
            name="email"
            value={credentials.email}
            onChange={handleChange}
            placeholder="correo@ejemplo.com"
            required
            style={{
              width: '100%',
              padding: '10px 14px',
              borderRadius: '8px',
              border: '1px solid var(--border-color)',
              background: 'var(--bg-input)',
              color: 'var(--text-main)',
              outline: 'none'
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
            Contraseña
          </label>
          <input
            type="password"
            name="password"
            value={credentials.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
            style={{
              width: '100%',
              padding: '10px 14px',
              borderRadius: '8px',
              border: '1px solid var(--border-color)',
              background: 'var(--bg-input)',
              color: 'var(--text-main)',
              outline: 'none'
            }}
          />
        </div>

        <button type="submit" className="btn-b2b-primary" style={{ marginTop: '8px', width: '100%' }} disabled={loading}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>

      <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
        ¿No tienes cuenta?{' '}
        {onSwitchToRegister ? (
          <button
            type="button"
            onClick={onSwitchToRegister}
            style={{ background: 'none', border: 'none', color: 'var(--text-accent)', fontWeight: '600', cursor: 'pointer' }}
          >
            Regístrate
          </button>
        ) : (
          <Link to="/register" style={{ color: 'var(--text-accent)', textDecoration: 'none', fontWeight: '600' }}>
            Regístrate
          </Link>
        )}
      </p>
    </div>
  );
};
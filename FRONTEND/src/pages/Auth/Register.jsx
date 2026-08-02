import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerService } from '../../services/authService';

export const Register = ({ onSwitchToLogin, onSuccess }) => {
  const [userType, setUserType] = useState('user');
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const roleMap = {
    user: 3, // PROFESSIONAL
    company: 4, // COMPANY
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
        email: formData.email,
        password: formData.password,
        roleId: roleMap[userType],
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

  return (
    <div style={{ width: '100%', maxWidth: '400px', margin: '0 auto', padding: '10px' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '8px' }}>Crear Cuenta</h2>
      <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: '24px', fontSize: '0.9rem' }}>
        Únete a la plataforma B2BMatch
      </p>

      {/* Selector de Tipo de Usuario */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button
          type="button"
          className={userType === 'user' ? 'btn-b2b-primary' : 'btn-b2b-outline'}
          style={{ flex: 1, padding: '8px', fontSize: '0.85rem' }}
          onClick={() => setUserType('user')}
        >
          👨‍💻 Postulante
        </button>
        <button
          type="button"
          className={userType === 'company' ? 'btn-b2b-primary' : 'btn-b2b-outline'}
          style={{ flex: 1, padding: '8px', fontSize: '0.85rem' }}
          onClick={() => setUserType('company')}
        >
          🏢 Empresa
        </button>
      </div>

      {error && <p style={{ color: 'red', padding: '0 10px' }}>{error}</p>}
      {success && <p style={{ color: 'green', padding: '0 10px' }}>{success}</p>}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
            {userType === 'user' ? 'Nombre Completo' : 'Nombre de la Empresa'}
          </label>
          <input 
            type="text" 
            name="name"
            value={formData.name}
            onChange={handleChange}
            placeholder={userType === 'user' ? 'Juan Pérez' : 'Acme B2B Corp'}
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
            Correo Electrónico
          </label>
          <input 
            type="email" 
            name="email"
            value={formData.email}
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
            value={formData.password}
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
          {loading ? 'Registrando...' : 'Registrarme'}
        </button>
      </form>

      <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
        ¿Ya tienes cuenta?{' '}
        {onSwitchToLogin ? (
          <button 
            type="button" 
            onClick={onSwitchToLogin} 
            style={{ background: 'none', border: 'none', color: 'var(--text-accent)', fontWeight: '600', cursor: 'pointer' }}
          >
            Inicia Sesión
          </button>
        ) : (
          <Link to="/login" style={{ color: 'var(--text-accent)', textDecoration: 'none', fontWeight: '600' }}>
            Inicia Sesión
          </Link>
        )}
      </p>
    </div>
  );
};

export default Register;
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import perfilesService from '../../services/perfilesService';

const inputStyle = {
  width: '100%',
  padding: '10px 14px',
  borderRadius: '8px',
  border: '1px solid var(--border-color)',
  background: 'var(--bg-input)',
  color: 'var(--text-main)',
  outline: 'none',
};

const labelStyle = {
  display: 'block',
  marginBottom: '6px',
  color: 'var(--text-muted)',
  fontSize: '0.9rem',
};

export const CompanyProfile = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    companyName: '',
    taxId: '',
    industry: '',
    website: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    country: '',
    companyDescription: '',
  });
  const [profileId, setProfileId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      if (!user) return;
      try {
        const data = await perfilesService.getCompanyProfileByUser(user.id);
        if (data) {
          setProfile({
            companyName: data.companyName || '',
            taxId: data.taxId || '',
            industry: data.industry || '',
            website: data.website || '',
            email: data.email || '',
            phone: data.phone || '',
            address: data.address || '',
            city: data.city || '',
            country: data.country || '',
            companyDescription: data.companyDescription || '',
          });
          setProfileId(data.id || null);
        } else {
          setProfile((p) => ({ ...p, email: user.email || '' }));
        }
      } catch (err) {
        console.error('Error cargando perfil empresa', err);
        setError('No se pudo cargar el perfil de tu empresa.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [user]);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      const payload = {
        userId: user.id,
        companyName: profile.companyName,
        taxId: profile.taxId,
        industry: profile.industry || null,
        website: profile.website || null,
        email: profile.email || user.email || null,
        phone: profile.phone || null,
        address: profile.address || null,
        city: profile.city || null,
        country: profile.country || null,
        companyDescription: profile.companyDescription || null,
      };
      if (profileId) {
        await perfilesService.updateCompanyProfile(profileId, payload);
      } else {
        await perfilesService.createCompanyProfile(payload);
      }
      alert('Perfil de empresa guardado');
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Error al guardar perfil de empresa');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <p style={{ padding: '20px' }}>Cargando perfil de empresa...</p>;
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Perfil de la Empresa</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Configura los datos públicos de tu organización</p>

      {error && (
        <div style={{ marginBottom: '20px', padding: '16px', background: '#fdecea', color: '#b91c1c', borderRadius: '12px' }}>
          {error}
        </div>
      )}

      <div className="card-b2b" style={{ padding: '32px' }}>
        <form onSubmit={(e) => e.preventDefault()} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={labelStyle}>Nombre de la Empresa</label>
            <input
              type="text"
              name="companyName"
              value={profile.companyName}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>

          <div>
            <label style={labelStyle}>CUIT / Tax ID</label>
            <input
              type="text"
              name="taxId"
              value={profile.taxId}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Industria / Sector</label>
              <input
                type="text"
                name="industry"
                value={profile.industry}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Sitio Web</label>
              <input
                type="url"
                name="website"
                value={profile.website}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Correo de Contacto</label>
              <input
                type="email"
                name="email"
                value={profile.email}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>Teléfono</label>
              <input
                type="text"
                name="phone"
                value={profile.phone}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
          </div>

          <div>
            <label style={labelStyle}>Dirección</label>
            <input
              type="text"
              name="address"
              value={profile.address}
              onChange={handleChange}
              style={inputStyle}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
            <div>
              <label style={labelStyle}>Ciudad</label>
              <input
                type="text"
                name="city"
                value={profile.city}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>País</label>
              <input
                type="text"
                name="country"
                value={profile.country}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>
          </div>

          <div>
            <label style={labelStyle}>Descripción de la Empresa</label>
            <textarea
              name="companyDescription"
              rows="4"
              value={profile.companyDescription}
              onChange={handleChange}
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <button type="button" className="btn-b2b-primary" style={{ marginTop: '10px', alignSelf: 'flex-start' }} onClick={handleSave} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar Cambios'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CompanyProfile;

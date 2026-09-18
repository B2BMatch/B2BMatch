import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import perfilesService from '../../services/perfilesService';
import '../../styles/profile.css';
import '../../styles/catalog.css';

const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] || '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase() || 'E';
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
    <div className="profile-page">
      <header className="page-head" style={{ padding: '0 0 24px' }}>
        <span className="badge-gold">Perfil de empresa</span>
        <h1>Perfil de la Empresa</h1>
        <p>Configurá los datos públicos de tu organización para publicar ofertas confiables.</p>
      </header>

      {error && (
        <div style={{ marginBottom: '20px', padding: '16px', background: '#fdecea', color: '#b91c1c', borderRadius: '12px' }}>
          {error}
        </div>
      )}

      <section className="profile-hero">
        <div className="profile-avatar-lg profile-avatar-lg--gold">
          {getInitials(profile.companyName || user?.name || 'E')}
        </div>
        <div className="profile-hero-info">
          <div className="profile-hero-name">
            {profile.companyName || user?.name || 'Tu empresa'}
            <span className="badge-verified">✓ Empresa verificada</span>
          </div>
          <p className="profile-hero-sub">
            {profile.companyDescription
              ? profile.companyDescription.slice(0, 150)
              : 'La descripción de tu empresa aparecerá aquí — completá el formulario abajo.'}
          </p>
          <div className="profile-hero-chips">
            {profile.industry && <span className="profile-hero-chip">🏭 {profile.industry}</span>}
            {(profile.city || profile.country) && (
              <span className="profile-hero-chip">📍 {profile.city}{profile.country ? `, ${profile.country}` : ''}</span>
            )}
            <span className="profile-hero-chip">🏢 Perfil público</span>
          </div>
        </div>
      </section>

      <section className="section-card">
        <h2>Datos de la organización</h2>
        <p className="section-sub">Esta información es pública y valida tu presencia en B2BMatch</p>
        <form onSubmit={(e) => e.preventDefault()} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="form-group">
            <label className="form-label">Nombre de la Empresa</label>
            <input type="text" className="form-input" name="companyName" value={profile.companyName} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label">CUIT / Tax ID</label>
            <input type="text" className="form-input" name="taxId" value={profile.taxId} onChange={handleChange} />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Industria / Sector</label>
              <input type="text" className="form-input" name="industry" value={profile.industry} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Sitio Web</label>
              <input type="url" className="form-input" name="website" value={profile.website} onChange={handleChange} />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Correo de Contacto</label>
              <input type="email" className="form-input" name="email" value={profile.email} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Teléfono</label>
              <input type="text" className="form-input" name="phone" value={profile.phone} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Dirección</label>
            <input type="text" className="form-input" name="address" value={profile.address} onChange={handleChange} />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Ciudad</label>
              <input type="text" className="form-input" name="city" value={profile.city} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">País</label>
              <input type="text" className="form-input" name="country" value={profile.country} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Descripción de la Empresa</label>
            <textarea className="form-textarea" name="companyDescription" rows="4" value={profile.companyDescription} onChange={handleChange} />
          </div>

          <div className="form-actions">
            <button type="button" className="btn-b2b-primary" onClick={handleSave} disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
};

export default CompanyProfile;
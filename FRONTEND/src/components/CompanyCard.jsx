import React from 'react';

export const CompanyCard = ({ name, industry, employees, description, onContact }) => {
  return (
    <div className="card-b2b">
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <span className="badge-tag">Empresa Aliada</span>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>👥 {employees}</span>
        </div>
        
        <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '6px', color: 'var(--text-title)' }}>
          {name}
        </h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-accent)', marginBottom: '12px', fontWeight: '600' }}>
          {industry}
        </p>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: '1.5', marginBottom: '20px' }}>
          {description}
        </p>
      </div>

      <button className="btn-b2b-outline" style={{ width: '100%' }} onClick={onContact}>
        Explorar Perfil
      </button>
    </div>
  );
};

export default CompanyCard;
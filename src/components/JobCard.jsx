import React from 'react';

export const JobCard = ({ title, company, location, salary, tags = [], onApply }) => {
  return (
    <div className="card-b2b">
      <div>
        {/* Badges / Etiquetas */}
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px' }}>
          {tags.map((tag, idx) => (
            <span key={idx} className="badge-tag">{tag}</span>
          ))}
        </div>

        {/* Título de la Oferta (Usa la variable adaptable) */}
        <h3 style={{ 
          fontSize: '1.25rem', 
          fontWeight: '700', 
          marginBottom: '8px', 
          color: 'var(--text-title)' 
        }}>
          {title}
        </h3>

        {/* Empresa y Ubicación */}
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '12px' }}>
          🏢 <strong style={{ color: 'var(--text-sub)' }}>{company}</strong> • 📍 {location}
        </p>

        {/* Salario */}
        {salary && (
          <p style={{ color: 'var(--text-accent)', fontWeight: '700', fontSize: '1.05rem', marginBottom: '20px' }}>
            {salary}
          </p>
        )}
      </div>

      <button className="btn-b2b-primary" style={{ width: '100%' }} onClick={onApply}>
        Postular Ahora 🚀
      </button>
    </div>
  );
};

export default JobCard;
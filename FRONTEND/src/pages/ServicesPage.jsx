import React, { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import catalogoService from '../services/catalogoService';
import perfilesService from '../services/perfilesService';
import quotationsService from '../services/quotationsService';
import SearchBar from '../components/SearchBar';
import SidebarFilters from '../components/SidebarFilters';
import Button from '../components/Button';
import '../styles/catalog.css';

const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] || '';
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase() || 'B2B';
};

const ServicesPage = () => {
  const { currentUser } = useAuth();
  const [services, setServices] = useState([]);
  const [categories, setCategories] = useState([]);
  const [professionals, setProfessionals] = useState([]);
  const [myQuotations, setMyQuotations] = useState([]);
  const [query, setQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quotationMessage, setQuotationMessage] = useState({});
  const [sendingServiceId, setSendingServiceId] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const [servicesList, cats, profs] = await Promise.all([
          catalogoService.getProfessionalServices(),
          catalogoService.getCategories(),
          perfilesService.getProfessionalProfiles(),
        ]);
        setServices(Array.isArray(servicesList) ? servicesList : []);
        setCategories(Array.isArray(cats) ? cats : []);
        setProfessionals(Array.isArray(profs) ? profs : []);
      } catch (err) {
        console.error('Error cargando servicios', err);
        setError('No se pudieron cargar los servicios.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    const loadMyQuotations = async () => {
      if (!currentUser?.id) return;
      try {
        const list = await quotationsService.getQuotationsByCustomer(currentUser.id);
        setMyQuotations(Array.isArray(list) ? list : []);
      } catch (err) {
        console.error('Error cargando mis cotizaciones', err);
      }
    };
    loadMyQuotations();
  }, [currentUser?.id]);

  const getCategoryName = (categoryId) => {
    return categories.find((c) => c.id === categoryId)?.name || 'General';
  };

  const getProfessionalName = (professionalId) => {
    const prof = professionals.find((p) => p.id === professionalId);
    if (prof?.firstName || prof?.lastName) return `${prof.firstName || ''} ${prof.lastName || ''}`.trim();
    return `Profesional #${professionalId}`;
  };

  const getServiceTitle = (serviceId) => {
    return services.find((s) => s.id === serviceId)?.title || `Servicio #${serviceId}`;
  };

  const filteredServices = useMemo(() => {
    return services.filter((service) => {
      const q = query.trim().toLowerCase();
      const matchesQuery = !q || (service.title || '').toLowerCase().includes(q) || (service.description || '').toLowerCase().includes(q);
      const matchesCategory = !categoryFilter || String(service.category_id) === String(categoryFilter);
      return matchesQuery && matchesCategory;
    });
  }, [services, query, categoryFilter]);

  const handleRequestQuotation = async (serviceId) => {
    if (!currentUser) {
      alert('Debes iniciar sesión para solicitar una cotización.');
      return;
    }
    const message = (quotationMessage[serviceId] || '').trim();
    if (!message) {
      alert('Escribe un mensaje describiendo lo que necesitas.');
      return;
    }

    setSendingServiceId(serviceId);
    try {
      await quotationsService.createQuotation({
        serviceId,
        customerId: currentUser.id,
        message,
      });
      alert('Cotización solicitada correctamente.');
      setQuotationMessage((prev) => ({ ...prev, [serviceId]: '' }));
      const list = await quotationsService.getQuotationsByCustomer(currentUser.id);
      setMyQuotations(Array.isArray(list) ? list : []);
    } catch (err) {
      console.error('Error solicitando cotización', err);
      alert('No se pudo solicitar la cotización. Intenta de nuevo.');
    } finally {
      setSendingServiceId(null);
    }
  };

  if (loading) return <p style={{ padding: '20px' }}>Cargando servicios...</p>;
  if (error) return <p style={{ padding: '20px', color: 'red' }}>{error}</p>;

  return (
    <div>
      <header className="page-head">
        <span className="badge-gold">Catálogo de servicios</span>
        <h1>Servicios Profesionales</h1>
        <p>Explorá servicios ofrecidos por profesionales verificados y solicitá una cotización.</p>
      </header>

      <div className="catalog-layout">
        <aside className="catalog-sidebar">
          <SearchBar placeholder="Buscar por título o descripción..." value={query} onChange={(e) => setQuery(e.target.value)} />
          <SidebarFilters categories={categories} value={categoryFilter} onChange={setCategoryFilter} />
        </aside>

        <div className="catalog-main">
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>
            {filteredServices.length} servicio{filteredServices.length === 1 ? '' : 's'} disponible{filteredServices.length === 1 ? '' : 's'}
          </p>

          {filteredServices.length === 0 ? (
            <div className="empty-state">
              <strong>Sin resultados</strong>
              <span>No hay servicios que coincidan con tu búsqueda o filtro de categoría.</span>
            </div>
          ) : (
            <div className="result-grid">
              {filteredServices.map((service) => {
                const providerName = getProfessionalName(service.professional_id);
                return (
                  <article key={service.id} className="gig-card">
                    <div className="gig-cover">
                      <span className="badge-gold">{getCategoryName(service.category_id)}</span>
                    </div>
                    <div className="gig-body">
                      <div className="gig-provider">
                        <span className={`gig-avatar gig-avatar--sm ${service.professional_id % 2 ? 'gig-avatar--navy' : 'gig-avatar--gold'}`}>
                          {getInitials(providerName)}
                        </span>
                        <div style={{ minWidth: 0 }}>
                          <p className="gig-provider-name">{providerName}</p>
                          <span className="badge-verified">✓ Profesional verificado</span>
                        </div>
                      </div>
                      <h3 className="gig-title">{service.title}</h3>
                      <p className="gig-desc">{service.description || 'Sin descripción.'}</p>
                      <div className="gig-meta">
                        <span className="badge-gold">Top</span>
                        <span className="gig-price">
                          <small>Desde</small>
                          {service.price != null ? `$${service.price}` : 'A convenir'}
                        </span>
                      </div>

                      {currentUser ? (
                        <div className="quote-box">
                          <textarea
                            className="form-textarea"
                            rows="2"
                            placeholder="Describe qué necesitas..."
                            value={quotationMessage[service.id] || ''}
                            onChange={(e) => setQuotationMessage((prev) => ({ ...prev, [service.id]: e.target.value }))}
                          />
                          <Button
                            variant="primary"
                            disabled={sendingServiceId === service.id}
                            onClick={() => handleRequestQuotation(service.id)}
                            style={{ marginTop: '8px', width: '100%' }}
                          >
                            {sendingServiceId === service.id ? 'Enviando...' : 'Solicitar Cotización'}
                          </Button>
                        </div>
                      ) : (
                        <Link to="/login" className="btn-pill btn-pill--outline">
                          Iniciar sesión para cotizar
                        </Link>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}

          {currentUser && (
            <section className="section-card">
              <h2>Mis Cotizaciones</h2>
              <p className="section-sub">Solicitudes de cotización que enviaste</p>
              {myQuotations.length === 0 ? (
                <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                  Todavía no solicitaste ninguna cotización.
                </p>
              ) : (
                <div className="item-list">
                  {myQuotations.map((quotation) => (
                    <div key={quotation.id} className="item-row">
                      <div className="item-row-header">
                        <span>{getServiceTitle(quotation.serviceId)}</span>
                        <span className="badge-gold">{quotation.status || 'PENDING'}</span>
                      </div>
                      <p>{quotation.message}</p>
                      <small>
                        Enviada el {quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString() : 'sin fecha'}
                      </small>
                    </div>
                  ))}
                </div>
              )}
            </section>
          )}
        </div>
      </div>
    </div>
  );
};

export default ServicesPage;
import React, { useEffect, useState, useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import catalogoService from '../services/catalogoService';
import perfilesService from '../services/perfilesService';
import quotationsService from '../services/quotationsService';
import SearchBar from '../components/SearchBar';
import Sidebar from '../components/Sidebar';
import SidebarFilters from '../components/SidebarFilters';
import Button from '../components/Button';

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
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '32px 20px' }}>
      <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>Servicios Profesionales</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>
        Explora servicios ofrecidos por profesionales y solicita una cotización
      </p>

      <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start', flexWrap: 'wrap' }}>
        <Sidebar>
          <SidebarFilters categories={categories} value={categoryFilter} onChange={setCategoryFilter} />
        </Sidebar>

        <div style={{ flex: 1, minWidth: '280px' }}>
          <div style={{ marginBottom: '20px' }}>
            <SearchBar placeholder="Buscar por título o descripción..." value={query} onChange={(e) => setQuery(e.target.value)} />
          </div>

          {filteredServices.length === 0 ? (
            <div className="card-b2b" style={{ padding: '24px' }}>
              <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                No hay servicios que coincidan con tu búsqueda.
              </p>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
              {filteredServices.map((service) => (
                <div key={service.id} className="card-b2b" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  <span className="badge-tag" style={{ alignSelf: 'flex-start' }}>
                    {getCategoryName(service.category_id)}
                  </span>
                  <h3 style={{ fontSize: '1.15rem', margin: 0 }}>{service.title}</h3>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: 0 }}>
                    Ofrecido por {getProfessionalName(service.professional_id)}
                  </p>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: 0, flex: 1 }}>
                    {service.description || 'Sin descripción.'}
                  </p>
                  <p style={{ fontWeight: '700', margin: 0, fontSize: '1.05rem' }}>
                    {service.price != null ? `$${service.price}` : 'Precio a convenir'}
                  </p>

                  {currentUser && (
                    <div style={{ marginTop: '6px' }}>
                      <textarea
                        rows="2"
                        placeholder="Describe qué necesitas..."
                        value={quotationMessage[service.id] || ''}
                        onChange={(e) => setQuotationMessage((prev) => ({ ...prev, [service.id]: e.target.value }))}
                        style={{
                          width: '100%',
                          boxSizing: 'border-box',
                          padding: '8px 12px',
                          borderRadius: '8px',
                          background: 'var(--bg-input)',
                          color: 'var(--text-main)',
                          border: '1px solid var(--border-color)',
                          resize: 'vertical',
                          outline: 'none',
                        }}
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
                  )}
                </div>
              ))}
            </div>
          )}

          {currentUser && (
            <div className="card-b2b" style={{ padding: '24px', marginTop: '28px' }}>
              <h2 style={{ fontSize: '1.3rem', marginBottom: '6px' }}>Mis Cotizaciones</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px' }}>
                Solicitudes de cotización que enviaste
              </p>
              {myQuotations.length === 0 ? (
                <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                  Todavía no solicitaste ninguna cotización.
                </p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {myQuotations.map((quotation) => (
                    <div key={quotation.id} style={{ border: '1px solid var(--border-color)', borderRadius: '10px', padding: '14px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px', marginBottom: '4px' }}>
                        <strong>{getServiceTitle(quotation.serviceId)}</strong>
                        <span className="badge-tag">{quotation.status || 'PENDING'}</span>
                      </div>
                      <p style={{ margin: '4px 0', color: 'var(--text-muted)', fontSize: '0.9rem' }}>{quotation.message}</p>
                      <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                        Enviada el {quotation.createdAt ? new Date(quotation.createdAt).toLocaleDateString() : 'sin fecha'}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ServicesPage;

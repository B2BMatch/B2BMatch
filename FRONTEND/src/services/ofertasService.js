import api from './api';

// Obtener todas las ofertas
export const getOfertas = async () => {
    const response = await api.get('/job-offers');
    return response.data;
};

// Obtener una oferta por id
export const getOfertaById = async (id) => {
    const response = await api.get(`/job-offers/${id}`);
    return response.data;
};

// Obtener ofertas por empresa (company profile id)
export const getOfertasByCompany = async (companyId) => {
    const response = await api.get(`/job-offers/company/${companyId}`);
    return response.data;
};

// Crear una nueva oferta
export const createOferta = async (ofertaData) => {
    const response = await api.post('/job-offers', ofertaData);
    return response.data;
};

// Actualizar una oferta
export const updateOferta = async (id, ofertaData) => {
    const response = await api.put(`/job-offers/${id}`, ofertaData);
    return response.data;
};

// Cambiar el estado de una oferta (Bajar Oferta / Activar)
export const updateOfertaStatus = async (id, status) => {
    const response = await api.patch(`/job-offers/${id}/status`, { status });
    return response.data;
};

// Eliminar (borrado lógico) una oferta
export const deleteOferta = async (id) => {
    await api.delete(`/job-offers/${id}`);
    return true;
};

export default {
    getOfertas,
    getOfertaById,
    getOfertasByCompany,
    createOferta,
    updateOferta,
    updateOfertaStatus,
    deleteOferta,
};

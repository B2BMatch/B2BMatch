import api from './api';

// Categories
export const getCategories = async () => {
  const res = await api.get('/catalogo/categories');
  return res.data;
};

export const createCategory = async (payload) => {
  const res = await api.post('/catalogo/categories', payload);
  return res.data;
};

// Skills
export const getSkills = async () => {
  const res = await api.get('/catalogo/skills');
  return res.data;
};

export const createSkill = async (payload) => {
  const res = await api.post('/catalogo/skills', payload);
  return res.data;
};

// Company services
export const getCompanyServices = async () => {
  const res = await api.get('/catalogo/company-services');
  return res.data;
};

export const createCompanyService = async (payload) => {
  const res = await api.post('/catalogo/company-services', payload);
  return res.data;
};

export const getCompanyServiceById = async (id) => {
  const res = await api.get(`/catalogo/company-services/${id}`);
  return res.data;
};

export const updateCompanyService = async (id, payload) => {
  const res = await api.put(`/catalogo/company-services/${id}`, payload);
  return res.data;
};

export const deleteCompanyService = async (id) => {
  const res = await api.delete(`/catalogo/company-services/${id}`);
  return res.data;
};

// Professional services
export const getProfessionalServices = async () => {
  const res = await api.get('/catalogo/professional-services');
  return res.data;
};

export const createProfessionalService = async (payload) => {
  const res = await api.post('/catalogo/professional-services', payload);
  return res.data;
};

export default {
  getCategories,
  createCategory,
  getSkills,
  createSkill,
  getCompanyServices,
  getCompanyServiceById,
  createCompanyService,
  updateCompanyService,
  deleteCompanyService,
  getProfessionalServices,
  createProfessionalService,
};

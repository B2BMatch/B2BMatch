import api from './api';

export const getQuotations = async () => {
  const res = await api.get('/quotations');
  return res.data;
};

export const getQuotationById = async (id) => {
  const res = await api.get(`/quotations/${id}`);
  return res.data;
};

export const getQuotationsByService = async (serviceId) => {
  const res = await api.get(`/quotations/service/${serviceId}`);
  return res.data;
};

export const getQuotationsByCustomer = async (customerId) => {
  const res = await api.get(`/quotations/customer/${customerId}`);
  return res.data;
};

export const createQuotation = async (payload) => {
  const res = await api.post('/quotations', payload);
  return res.data;
};

export const updateQuotation = async (id, payload) => {
  const res = await api.put(`/quotations/${id}`, payload);
  return res.data;
};

export const deleteQuotation = async (id) => {
  await api.delete(`/quotations/${id}`);
  return true;
};

export default {
  getQuotations,
  getQuotationById,
  getQuotationsByService,
  getQuotationsByCustomer,
  createQuotation,
  updateQuotation,
  deleteQuotation,
};

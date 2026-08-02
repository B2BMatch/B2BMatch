import api from './api';

export const getReviews = async () => {
  const res = await api.get('/reviews');
  return res.data;
};

export const getReviewById = async (id) => {
  const res = await api.get(`/reviews/${id}`);
  return res.data;
};

export const getReviewsByProfessional = async (professionalId) => {
  const res = await api.get(`/reviews/professional/${professionalId}`);
  return res.data;
};

export const getReviewsByCustomer = async (customerId) => {
  const res = await api.get(`/reviews/customer/${customerId}`);
  return res.data;
};

export const createReview = async (payload) => {
  const res = await api.post('/reviews', payload);
  return res.data;
};

export const updateReview = async (id, payload) => {
  const res = await api.put(`/reviews/${id}`, payload);
  return res.data;
};

export const deleteReview = async (id) => {
  await api.delete(`/reviews/${id}`);
  return true;
};

export default {
  getReviews,
  getReviewById,
  getReviewsByProfessional,
  getReviewsByCustomer,
  createReview,
  updateReview,
  deleteReview,
};

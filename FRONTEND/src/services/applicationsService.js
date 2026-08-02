import api from './api';

export const getApplications = async () => {
  const res = await api.get('/job-applications');
  return res.data;
};

export const getApplicationsByProfessionalId = async (professionalId) => {
  const res = await api.get(`/job-applications/professional/${professionalId}`);
  return res.data;
};

export const getApplicationsByJobOfferId = async (jobOfferId) => {
  const res = await api.get(`/job-applications/job-offer/${jobOfferId}`);
  return res.data;
};

export const createApplication = async (payload) => {
  const res = await api.post('/job-applications', payload);
  return res.data;
};

export default {
  getApplications,
  getApplicationsByProfessionalId,
  getApplicationsByJobOfferId,
  createApplication,
};

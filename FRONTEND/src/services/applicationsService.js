import api from './api';

export const getApplications = async () => {
  const res = await api.get('/job-applications');
  return res.data;
};

export const getApplicationsByUserId = async (userId) => {
  const res = await api.get(`/job-applications/user/${userId}`);
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

export const acceptApplication = async (id) => {
  const res = await api.patch(`/job-applications/${id}/accept`);
  return res.data;
};

export const rejectApplication = async (id) => {
  const res = await api.patch(`/job-applications/${id}/reject`);
  return res.data;
};

export default {
  getApplications,
  getApplicationsByUserId,
  getApplicationsByJobOfferId,
  createApplication,
  acceptApplication,
  rejectApplication,
};

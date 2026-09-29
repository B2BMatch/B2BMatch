import api from './api';

// Company profiles
export const getCompanyProfiles = async () => {
  const res = await api.get('/company-profiles');
  return res.data;
};

export const getCompanyProfileByUser = async (userId) => {
  try {
    const res = await api.get(`/company-profiles/user/${userId}`);
    return res.data;
  } catch (err) {
    if (err.response?.status === 404) return null;
    throw err;
  }
};

export const createCompanyProfile = async (payload) => {
  const res = await api.post('/company-profiles', payload);
  return res.data;
};

export const updateCompanyProfile = async (id, payload) => {
  const res = await api.put(`/company-profiles/${id}`, payload);
  return res.data;
};

export const deleteCompanyProfile = async (id) => {
  await api.delete(`/company-profiles/${id}`);
  return true;
};

// Professional profiles
export const getProfessionalProfiles = async () => {
  const res = await api.get('/professional-profiles');
  return res.data;
};

export const getProfessionalProfileByUser = async (userId) => {
  try {
    const res = await api.get(`/professional-profiles/user/${userId}`);
    return res.data;
  } catch (err) {
    if (err.response?.status === 404) return null;
    throw err;
  }
};

export const createProfessionalProfile = async (payload) => {
  const res = await api.post('/professional-profiles', payload);
  return res.data;
};

export const updateProfessionalProfile = async (id, payload) => {
  const res = await api.put(`/professional-profiles/${id}`, payload);
  return res.data;
};

export const deleteProfessionalProfile = async (id) => {
  await api.delete(`/professional-profiles/${id}`);
  return true;
};

export default {
  // company
  getCompanyProfiles,
  getCompanyProfileByUser,
  createCompanyProfile,
  updateCompanyProfile,
  deleteCompanyProfile,
  // professional
  getProfessionalProfiles,
  getProfessionalProfileByUser,
  createProfessionalProfile,
  updateProfessionalProfile,
  deleteProfessionalProfile,
};

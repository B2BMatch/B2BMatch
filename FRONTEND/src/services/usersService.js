import api from './api';

export const getUsers = async (params = {}) => {
  const res = await api.get('/users', { params });
  return res.data;
};

export const deleteUser = async (id) => {
  await api.delete(`/users/${id}`);
  return true;
};

export const reactivateUser = async (id) => {
  const res = await api.patch(`/users/${id}/reactivate`);
  return res.data;
};

export const updateUserStatus = async (id, status) => {
  const res = await api.patch(`/users/${id}/status`, { status });
  return res.data;
};

export default { getUsers, deleteUser, reactivateUser, updateUserStatus };

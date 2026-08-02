import api from './api';

export const getUsers = async () => {
  const res = await api.get('/users');
  return res.data;
};

export const getUsersByRole = async (roleName) => {
  const res = await api.get(`/users/role/${roleName}`);
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

export default { getUsers, getUsersByRole, deleteUser, reactivateUser, updateUserStatus };

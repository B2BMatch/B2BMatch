import api from './api';

export const getNotificationsByUser = async (userId) => {
  const res = await api.get(`/notifications/user/${userId}`);
  return res.data;
};

export const createNotification = async (payload) => {
  const res = await api.post('/notifications', payload);
  return res.data;
};

export const markAsRead = async (id) => {
  const res = await api.patch(`/notifications/${id}/read`);
  return res.data;
};

export const deleteNotification = async (id) => {
  await api.delete(`/notifications/${id}`);
  return true;
};

export default {
  getNotificationsByUser,
  createNotification,
  markAsRead,
  deleteNotification,
};

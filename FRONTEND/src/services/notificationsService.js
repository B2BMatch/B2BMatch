import api from './api';

export const getNotifications = async () => {
  const res = await api.get('/notifications');
  return res.data;
};

export const getNotificationsByUser = async (userId) => {
  const res = await api.get(`/notifications/user/${userId}`);
  return res.data;
};

export const getUnreadNotifications = async (userId) => {
  const res = await api.get(`/notifications/user/${userId}/unread`);
  return res.data;
};

export const countUnreadNotifications = async (userId) => {
  const res = await api.get(`/notifications/user/${userId}/unread-count`);
  return res.data;
};

export const createNotification = async (payload) => {
  const res = await api.post('/notifications', payload);
  return res.data;
};

export const markAsRead = async (id) => {
  const res = await api.post(`/notifications/${id}/read`);
  return res.data;
};

export const deleteNotification = async (id) => {
  await api.delete(`/notifications/${id}`);
  return true;
};

export default {
  getNotifications,
  getNotificationsByUser,
  getUnreadNotifications,
  countUnreadNotifications,
  createNotification,
  markAsRead,
  deleteNotification,
};

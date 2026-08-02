import api from './api';

/**
 * Inicia sesión con las credenciales del usuario.
 * @param {Object} credentials - { email, password }
 */
export const login = async (credentials) => {
    const response = await api.post('/users/login', credentials);
    return response.data; // Esperamos que devuelva los datos del usuario
};

/**
 * Registra un nuevo usuario en el sistema.
 * @param {Object} userData - Datos de registro
 */
export const register = async (userData) => {
    const response = await api.post('/users/register', userData);
    return response.data;
};
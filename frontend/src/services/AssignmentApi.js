import { apiClient } from './apiClient';
const BASE_PATH = '/api/assignments';

export const fetchAssignments = () => apiClient(BASE_PATH);
export const fetchAssignmentById = (id) => apiClient(`${BASE_PATH}/${id}`);
export const deleteAssignment = (id) => apiClient(`${BASE_PATH}/${id}`, { method: 'DELETE' });
export const createAssignment = (data) => apiClient(BASE_PATH, { method: 'POST', body: JSON.stringify(data) });
export const updateAssignment = (id, data) => apiClient(`${BASE_PATH}/${id}`, { method: 'PUT', body: JSON.stringify(data) });
export const optimizeAssignmentRoute = (id) => apiClient(`${BASE_PATH}/${id}/optimize`, { method: 'POST' });
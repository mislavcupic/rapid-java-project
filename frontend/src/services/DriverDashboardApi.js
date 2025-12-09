// frontend/src/services/DriverDashboardApi.js

import { apiClient } from './apiClient';

const BASE_ASSIGNMENT_PATH = '/api/assignments'; // ✅ PROMIJENJENO

// 1. DOHVAĆANJE RASPOREDA ZA PRIJAVLJENOG VOZAČA
export const fetchMySchedule = async () => {
    try {
        const data = await apiClient(`${BASE_ASSIGNMENT_PATH}/my-schedule`, { method: 'GET' }); // ✅ ISPRAVLJENO
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju rasporeda vozača:", error);
        return []; // ✅ Vraća [] umjesto throw
    }
};

// 2. DOHVAĆANJE DETALJA ASSIGNMENTA
export const fetchAssignmentDetails = async (id) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${id}`, { method: 'GET' });
    } catch (error) {
        console.error(`Greška pri dohvaćanju detalja assignmenta ${id}:`, error);
        throw error;
    }
};

// 3. POKRETANJE ASSIGNMENTA
export const startAssignment = async (id) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/start`, { method: 'PUT' }); // ✅ PUT, ne POST
    } catch (error) {
        console.error(`Greška pri pokretanju assignmenta ${id}:`, error);
        throw error;
    }
};

// 4. ZAVRŠAVANJE ASSIGNMENTA
export const completeAssignment = async (id) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/complete`, { method: 'PUT' }); // ✅ PUT, ne POST
    } catch (error) {
        console.error(`Greška pri završavanju assignmenta ${id}:`, error);
        throw error;
    }
};

// 5. POKRETANJE DOSTAVE POŠILJKE
export const startDelivery = async (assignmentId, shipmentId) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${assignmentId}/shipments/${shipmentId}/start`, { method: 'POST' });
    } catch (error) {
        console.error(`Greška pri pokretanju dostave ${shipmentId}:`, error);
        throw error;
    }
};

// 6. ZAVRŠAVANJE DOSTAVE (POD)
export const completeDelivery = async (assignmentId, shipmentId, podData) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${assignmentId}/shipments/${shipmentId}/complete`, {
            method: 'POST',
            body: JSON.stringify(podData)
        });
    } catch (error) {
        console.error(`Greška pri završavanju dostave ${shipmentId}:`, error);
        throw error;
    }
};
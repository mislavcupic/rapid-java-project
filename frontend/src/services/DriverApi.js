// frontend/src/services/DriverApi.js


import { apiClient } from './apiClient';

// Relativna putanja do glavnog resursa
const BASE_PATH = '/api/drivers';

// UKLONJENO: const getToken, const BASE_API_URL, const DRIVERS_URL, const handleResponse


/**
 * Dohvaća sve vozače.
 */
export const fetchDrivers = async () => {
    try {
        const data = await apiClient(BASE_PATH, {
            method: 'GET',
        });

        // ✅ KRITIČNA KOREKCIJA: Osiguravamo da se za listu uvijek vraća niz.
        return Array.isArray(data) ? data : [];

    } catch (error) {
        console.error("Greška pri dohvaćanju vozača:", error);
        // Propagiramo grešku dalje da je komponenta može uhvatiti
        throw error;
    }
};

/**
 * Dohvaća vozača po ID-ju.
 */
export const fetchDriverById = async (id) => {
    const path = `${BASE_PATH}/${id}`;

    try {
        return await apiClient(path, {
            method: 'GET',
        });
    } catch (error) {
        console.error(`Greška pri dohvaćanju vozača ID:${id}:`, error);
        throw error;
    }
};

/**
 * Kreira novog vozača.
 */
export const createDriver = async (driverData) => {
    try {
        return await apiClient(BASE_PATH, {
            method: 'POST',
            // apiClient automatski postavlja Content-Type: application/json
            body: JSON.stringify(driverData)
        });
    } catch (error) {
        console.error("Greška pri kreiranju vozača:", error);
        throw error;
    }
};

/**
 * Ažurira postojećeg vozača.
 */
export const updateDriver = async (id, driverData) => {
    const path = `${BASE_PATH}/${id}`;

    try {
        return await apiClient(path, {
            method: 'PUT',
            body: JSON.stringify(driverData)
        });
    } catch (error) {
        console.error(`Greška pri ažuriranju vozača ID:${id}:`, error);
        throw error;
    }
};

/**
 * Briše vozača.
 */
export const deleteDriver = async (id) => {
    const path = `${BASE_PATH}/${id}`;

    try {
        // apiClient automatski vraća null ili {} za 204 No Content
        return await apiClient(path, {
            method: 'DELETE',
        });
    } catch (error) {
        console.error(`Greška pri brisanju vozača ID:${id}:`, error);
        throw error;
    }
};
export const fetchDriverSchedule = async () => {
    try {
        const data = await apiClient('/api/assignments/my-schedule', {
            method: 'GET',
        });
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju rasporeda vozača:", error);
        return [];
    }
};
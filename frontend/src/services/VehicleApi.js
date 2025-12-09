// frontend/src/services/VehicleApi.js

import { apiClient } from './apiClient';

const BASE_VEHICLES_PATH = '/api/vehicles';
const BASE_DRIVERS_PATH = '/api/drivers';
const BASE_ANALYTICS_PATH = '/api/analytics/vehicles';
// Pretpostavka: Ako su fetchOverdueVehicles, fetchWarningVehicles, fetchFreeVehiclesDetails
// u AnalyticsPage.jsx importirani odavde, putanja je BASE_ANALYTICS_PATH

// =================================================================
// VEHICLE I DRIVER CRUD FUNKCIJE
// =================================================================

// 1. DOHVAĆANJE SVIH VOZILA (GET)
export const fetchVehicles = async () => {
    try {
        const data = await apiClient(BASE_VEHICLES_PATH, { method: 'GET' });
        // ✅ KRITIČNA KOREKCIJA: Osiguraj da je povratna vrijednost uvijek niz.
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju vozila:", error);
        throw error;
    }
};

// 2. DOHVAĆANJE SVIH VOZAČA (GET)
export const fetchDrivers = async () => {
    try {
        const data = await apiClient(BASE_DRIVERS_PATH, { method: 'GET' });
        // ✅ KRITIČNA KOREKCIJA: Osiguraj da je povratna vrijednost uvijek niz.
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju vozača:", error);
        throw error;
    }
};

// 3. DOHVAĆANJE VOZILA PO ID-ju (GET by ID)
export const fetchVehicleById = async (id) => {
    return apiClient(`${BASE_VEHICLES_PATH}/${id}`, { method: 'GET' });
};

// 4. KREIRANJE VOZILA (POST)
export const createVehicle = async (vehicleData) => {
    return apiClient(BASE_VEHICLES_PATH, {
        method: 'POST',
        body: JSON.stringify(vehicleData)
    });
}

// 5. AŽURIRANJE VOZILA (PUT)
export const updateVehicle = async (id, vehicleData) => {
    return apiClient(`${BASE_VEHICLES_PATH}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(vehicleData)
    });
};

// 6. BRISANJE VOZILA (DELETE)
export const deleteVehicle = async (id) => {
    return apiClient(`${BASE_VEHICLES_PATH}/${id}`, { method: 'DELETE' });
};


export const fetchOverdueVehicles = async () => {
    try {
        const data = await apiClient(`${BASE_ANALYTICS_PATH}/overdue`, { method: 'GET' });
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju vozila za servis:", error);
        throw error;
    }
};

export const fetchWarningVehicles = async () => {
    try {
        const data = await apiClient(`${BASE_ANALYTICS_PATH}/warning`, { method: 'GET' });
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju vozila s upozorenjem:", error);
        throw error;
    }
};

export const fetchFreeVehiclesDetails = async () => {
    try {
        const data = await apiClient(`${BASE_ANALYTICS_PATH}/free`, { method: 'GET' });
        // Vrati niz ili prazan niz.
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju slobodnih vozila:", error);
        throw error;
    }
};
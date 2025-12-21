// frontend/src/services/AnalyticsService.js

// ✅ IMPORTIRANJE CENTRALNOG API KLIJENTA
import { apiClient } from './apiClient';

// Relativne putanje do resursa
const BASE_PATH_SHIPMENTS = '/api/analytics/shipments';
const BASE_PATH_VEHICLES = '/api/analytics/vehicles';


// =================================================================
// 1. ANALITIKA POŠILJKI
// =================================================================

/**
 * Dohvaća prosječnu težinu aktivnih pošiljaka.
 * Očekivani povrat: broj (npr. 1500.5) ili objekt.
 */
export const getAverageActiveShipmentWeight = async () => {
    const path = `${BASE_PATH_SHIPMENTS}/average-active-weight`;

    try {
        // Očekuje se pojedinačna vrijednost, ne niz
        return await apiClient(path, {
            method: 'GET',
        });
    } catch (error) {
        console.error("Greška pri dohvaćanju prosječne težine pošiljaka:", error);
        // Osiguravamo da se greška propagira
        throw error;
    }
};

/**
 * Bulk akcija: označavanje pošiljaka kao zakasnjele.
 * Očekivani povrat: statusni objekt/poruka.
 */
export const bulkMarkOverdue = async () => {
    const path = `${BASE_PATH_SHIPMENTS}/mark-overdue`;

    try {
        // Očekuje se statusni objekt/poruka
        return await apiClient(path, {
            method: 'POST',
        });
    } catch (error) {
        console.error("Greška pri bulk akciji označavanja pošiljaka:", error);
        // Osiguravamo da se greška propagira
        throw error;
    }
};


// =================================================================
// 2. ANALITIKA VOZILA
// =================================================================

/**
 * Dohvaća statusnu analitiku vozila (npr. overdue, warning, free, total).
 * Očekivani povrat: objekt (npr. { overdue: 5, warning: 3, free: 10, total: 18 })
 */
export const fetchVehicleAnalytics = async () => {
    const path = `${BASE_PATH_VEHICLES}/status`;

    try {
        const data = await apiClient(path, {
            method: 'GET',
        });

        // Očekuje se objekt s brojevima, a ne niz, pa ne treba Array.isArray provjera
        return data;

    } catch (error) {
        console.error("Greška pri dohvaćanju statusne analitike vozila:", error);
        // Osiguravamo da se greška propagira
        throw error;
    }
};
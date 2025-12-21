// frontend/src/services/DriverDashboardApi.js

import { apiClient } from './apiClient'; // ✅ Named import

const BASE_ASSIGNMENT_PATH = '/api/assignments';
const BASE_SHIPMENT_PATH = '/api/shipments'; // ✅ DODANO za direktne Shipment endpointe

// 1. DOHVAĆANJE RASPOREDA ZA PRIJAVLJENOG VOZAČA
export const fetchMySchedule = async () => {
    try {
        const data = await apiClient(`${BASE_ASSIGNMENT_PATH}/my-schedule`, { method: 'GET' });
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju rasporeda vozača:", error);
        return [];
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

// 3. POKRETANJE ASSIGNMENTA (SCHEDULED → IN_PROGRESS)
export const startAssignment = async (id) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/start`, { method: 'PUT' });
    } catch (error) {
        console.error(`Greška pri pokretanju assignmenta ${id}:`, error);
        throw error;
    }
};

// 4. ZAVRŠAVANJE ASSIGNMENTA (IN_PROGRESS → COMPLETED)
export const completeAssignment = async (id) => {
    try {
        return await apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/complete`, { method: 'PUT' });
    } catch (error) {
        console.error(`Greška pri završavanju assignmenta ${id}:`, error);
        throw error;
    }
};

// 5. POKRETANJE DOSTAVE POŠILJKE (SCHEDULED → IN_TRANSIT)
// ✅ DIREKTNO NA /api/shipments/{shipmentId}/start
export const startDelivery = async (shipmentId) => {
    try {
        return await apiClient(
            `${BASE_SHIPMENT_PATH}/${shipmentId}/start`,
            { method: 'PUT' }
        );
    } catch (error) {
        console.error(`Greška pri pokretanju dostave ${shipmentId}:`, error);
        throw error;
    }
};

// 6. ZAVRŠAVANJE DOSTAVE (POD - Proof of Delivery, IN_TRANSIT → DELIVERED)
// ✅ DIREKTNO NA /api/shipments/{shipmentId}/complete
export const completeDelivery = async (shipmentId, podData) => {
    try {
        return await apiClient(
            `${BASE_SHIPMENT_PATH}/${shipmentId}/complete`,
            {
                method: 'POST',
                body: JSON.stringify(podData)
            }
        );
    } catch (error) {
        console.error(`Greška pri završavanju dostave ${shipmentId}:`, error);
        throw error;
    }
};

// 7. PRIJAVA PROBLEMA S DOSTAVOM (IN_TRANSIT → DELAYED)
// ✅ DIREKTNO NA /api/shipments/{shipmentId}/report-issue
export const reportIssue = async (shipmentId, issueData) => {
    try {
        return await apiClient(
            `${BASE_SHIPMENT_PATH}/${shipmentId}/report-issue`,
            {
                method: 'PUT',
                body: JSON.stringify(issueData)
            }
        );
    } catch (error) {
        console.error(`Greška pri prijavi problema ${shipmentId}:`, error);
        throw error;
    }
};
// frontend/src/services/DriverDashboardApi.js

import { apiClient } from './apiClient';

const BASE_ASSIGNMENT_PATH = '/api/assignments';
const BASE_SHIPMENT_PATH = '/api/shipments';

// Pomoćna funkcija za dohvat trenutnog Driver ID-a (pretpostavka da je spremljen pri prijavi)
const getCurrentDriverId = () => localStorage.getItem('driverId') || localStorage.getItem('userId');

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

// 2. DOHVAĆANJE DETALJA ASSIGNMENTA (Zbirna vožnja sa svim pošiljkama)
export const fetchAssignmentDetails = async (id) => {
    return apiClient(`${BASE_ASSIGNMENT_PATH}/${id}`, { method: 'GET' });
};

// 3. POKRETANJE ASSIGNMENTA (Cijela ruta)
export const startAssignment = async (id) => {
    const driverId = getCurrentDriverId();
    // Šaljemo driverId kao query parametar jer ga backend traži u @PostMapping
    return apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/start?driverId=${driverId}`, {
        method: 'POST'
    });
};

// 4. ZAVRŠAVANJE ASSIGNMENTA
export const completeAssignment = async (id) => {
    return apiClient(`${BASE_ASSIGNMENT_PATH}/${id}/complete`, { method: 'POST' });
};

// 5. POKRETANJE DOSTAVE POJEDINAČNE POŠILJKE
// Usklađeno s tvojim ShipmentService: startDelivery(Long shipmentId, Long driverId)
export const startDelivery = async (shipmentId) => {
    const driverId = getCurrentDriverId();
    return apiClient(
        `${BASE_SHIPMENT_PATH}/${shipmentId}/start?driverId=${driverId}`,
        { method: 'POST' }
    );
};

// 6. ZAVRŠAVANJE DOSTAVE (Proof of Delivery)
// Usklađeno s: completeDelivery(Long shipmentId, Long driverId, ProofOfDeliveryDTO pod)
export const completeDelivery = async (shipmentId, podData) => {
    const driverId = getCurrentDriverId();
    return apiClient(
        `${BASE_SHIPMENT_PATH}/${shipmentId}/complete`,
        {
            method: 'POST',
            body: JSON.stringify({
                ...podData,
                driverId: driverId // Šaljemo driverId unutar DTO-a
            })
        }
    );
};

// 7. PRIJAVA PROBLEMA S DOSTAVOM
// Usklađeno s: reportIssue(Long shipmentId, Long driverId, IssueReportDTO issue)
export const reportIssue = async (shipmentId, issueData) => {
    const driverId = getCurrentDriverId();
    return apiClient(
        `${BASE_SHIPMENT_PATH}/${shipmentId}/report-issue`,
        {
            method: 'POST',
            body: JSON.stringify({
                ...issueData,
                driverId: driverId
            })
        }
    );
};
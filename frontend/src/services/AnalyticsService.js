// src/services/AnalyticsService.js

const API_BASE_URL = 'http://localhost:8080/api/analytics';

const handleResponse = async (response) => {
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `HTTP error! Status: ${response.status}`);
    }
    return response;
};

// Funkcija sada samo koristi primljeni token (ne zna odakle je došao)
export const getAverageActiveShipmentWeight = async (token) => {
    const response = await fetch(`${API_BASE_URL}/shipments/average-active-weight`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    const validatedResponse = await handleResponse(response);
    return validatedResponse.json();
};

// Ista stvar za bulk update
export const bulkMarkOverdue = async (token) => {
    const response = await fetch(`${API_BASE_URL}/shipments/mark-overdue`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    const validatedResponse = await handleResponse(response);
    return validatedResponse.text();
};
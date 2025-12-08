// src/services/AnalyticsService.js (AŽURIRANA VERZIJA)

const API_BASE_URL = 'http://localhost:8080/api/analytics';

const handleResponse = async (response) => {
    if (!response.ok) {
        const errorText = await response.text();
        // Pokušaj parsiranja JSON-a ako je moguće za detaljniju grešku
        try {
            const errorDetail = await response.json();
            throw new Error(errorDetail.message || `HTTP error! Status: ${response.status}`);
        } catch (parseError) {
            // Response nije JSON format, koristi raw text
            throw new Error(errorText || `HTTP error! Status: ${response.status}. Parse error: ${parseError.message}`);
        }
    }
    return response;
};


// 1. Analitika pošiljki

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

// 2. Bulk operacija (bulk overdue)
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


//  3. ANALITIKA VOZILA (MAINTENANCE I SCHEDULER ALERT)
// Pretpostavljamo Backend endpoint: /api/analytics/vehicles/status
// Vraća DTO: { overdue: 2, warning: 5, free: 12, total: 20 }

export const fetchVehicleAnalytics = async (token) => {
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${API_BASE_URL}/vehicles/status`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    const validatedResponse = await handleResponse(response);
    return validatedResponse.json();
};

export const fetchVehicleAlertStatus = async (token) => {
    const response = await fetch(`${API_BASE_URL}/vehicles/status`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    const validatedResponse = await handleResponse(response);
    // Očekuje se JSON objekt tipa VehicleAnalyticsResponse
    return validatedResponse.json();
};
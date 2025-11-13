const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api';

// Pomoćna funkcija za obradu grešaka
const handleResponse = async (response) => {
    if (!response.ok) {
        let errorDetail = {};
        try {
            errorDetail = await response.json();
        } catch (e) {
            errorDetail.message = response.statusText;
        }
        throw new Error(errorDetail.message || `Greška [${response.status}]: ${response.statusText}`);
    }
    return response.status === 204 ? null : response.json();
};


// DRIVER DASHBOARD - Dohvaćanje svojih Assignment-a


/**
 * Dohvaća listu Assignment-a za trenutno ulogiranog Driver-a
 * GET /api/assignments/my-schedule
 */
export const fetchMySchedule = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/assignments/my-schedule`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

/**
 * Dohvaća detalje jednog Assignment-a
 * GET /api/assignments/{id}
 */
export const fetchAssignmentDetails = async (assignmentId) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/assignments/${assignmentId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

// ========================================================================
// ASSIGNMENT AKCIJE - Driver mijenja status Assignment-a
// ========================================================================

/**
 * Driver započinje Assignment (SCHEDULED → IN_PROGRESS)
 * PUT /api/assignments/{id}/start
 */
export const startAssignment = async (assignmentId) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/assignments/${assignmentId}/start`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

/**
 * Driver završava Assignment (IN_PROGRESS → COMPLETED)
 * PUT /api/assignments/{id}/complete
 */
export const completeAssignment = async (assignmentId) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/assignments/${assignmentId}/complete`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

// ========================================================================
// SHIPMENT AKCIJE - Driver mijenja status Shipment-a
// ========================================================================

/**
 * Driver započinje dostavu (SCHEDULED → IN_TRANSIT)
 * PUT /api/shipments/{id}/start
 */
export const startDelivery = async (shipmentId) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/shipments/${shipmentId}/start`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

/**
 * Driver završava dostavu s Proof of Delivery (IN_TRANSIT → DELIVERED)
 * POST /api/shipments/{id}/complete
 */
export const completeDelivery = async (shipmentId, podData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/shipments/${shipmentId}/complete`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(podData)
    });
    return handleResponse(response);
};

/**
 * Driver prijavljuje problem s dostavom (IN_TRANSIT → DELAYED)
 * PUT /api/shipments/{id}/report-issue
 */
export const reportDeliveryIssue = async (shipmentId, issueData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/shipments/${shipmentId}/report-issue`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(issueData)
    });
    return handleResponse(response);
};
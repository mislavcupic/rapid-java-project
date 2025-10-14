// frontend/src/services/ShipmentApi.js

const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api/shipments';

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
    return response.status !== 204 ? response.json() : null;
};

// 1. DOHVAĆANJE SVIH POŠILJKI (GET)
export const fetchShipments = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(BASE_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

// 2. DOHVAĆANJE POŠILJKE PO ID-ju (GET by ID)
export const fetchShipmentById = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};

// 3. KREIRANJE POŠILJKE (POST)
export const createShipment = async (shipmentData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(shipmentData)
    });
    return handleResponse(response);
}

// 4. AŽURIRANJE POŠILJKE (PUT)
export const updateShipment = async (id, shipmentData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(shipmentData)
    });
    return handleResponse(response);
};

// 5. BRISANJE POŠILJKE (DELETE)
export const deleteShipment = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return handleResponse(response);
};
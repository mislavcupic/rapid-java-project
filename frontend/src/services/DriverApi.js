// frontend/src/services/DriverApi.js

const getToken = () => localStorage.getItem('accessToken');
// Koristimo širi BASE_API_URL za druge entitete
const BASE_API_URL = 'http://localhost:8080/api';
const DRIVERS_URL = `${BASE_API_URL}/drivers`; // URL za vozače
// const USERS_URL = `${BASE_API_URL}/users`; // Ova konstanta se može ukloniti ako se nigdje ne koristi.

// Pomoćna funkcija za obradu grešaka (ostaje ista)
const handleResponse = async (response) => {
    if (!response.ok) {
        let errorDetail = {};
        try {
            errorDetail = await response.json();
        } catch (e) {
            console.error(e);
            errorDetail.message = response.statusText;
        }
        // Baci grešku s porukom backenda ili generičkom porukom
        throw new Error(errorDetail.message || `Greška [${response.status}]: ${response.statusText}`);
    }
        return response.status ===204 ? null : response.json();
};

//drivers
export const fetchDrivers = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(DRIVERS_URL, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    return handleResponse(response);
};

export const fetchDriverById = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(`${DRIVERS_URL}/${id}`, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    return handleResponse(response);
};

export const createDriver = async (driverData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(DRIVERS_URL, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(driverData)
    });
    return handleResponse(response);
};

export const updateDriver = async (id, driverData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(`${DRIVERS_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(driverData)
    });
    return handleResponse(response);
};

export const deleteDriver = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(`${DRIVERS_URL}/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    return handleResponse(response);
};



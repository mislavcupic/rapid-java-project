// frontend/src/services/VehicleApi.js (KONAČNA KOMPLETNA VERZIJA)
const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api/vehicles';

// --- Pomoćna funkcija za obradu odgovora (Handle Response) ---
// Rješava 204 No Content situacije i parsa JSON/Text greške
const handleResponse = async (response) => {
    // 1. Provjera je li odgovor uspješan (200-299)
    if (!response.ok) {
        // Pokušaj parsiranja JSON greške
        try {
            const errorJson = await response.json();
            // Bacamo grešku s porukom iz Backenda
            throw new Error(errorJson.message || `HTTP greška! Status: ${response.status}`);
        } catch (e) {
            // Ako nije JSON, pročitaj tekst i baci grešku
            console.error('Error getting access token', e);
            const errorText = await response.text();
            throw new Error(errorText || `HTTP greška! Status: ${response.status}`);
        }
    }

    // 2. Provjera 204 No Content (prazan odgovor)
    if (response.status === 204 || response.headers.get('content-length') === '0') {
        return null; // Vraćamo null/praznu vrijednost za 204
    }

    // 3. Vraćamo parsirani JSON za 200/201
    return response.json();
};

// =========================================================================
// 1. DOHVAĆANJE SVIH VOZILA (GET)
// =========================================================================
export const fetchVehicles = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen. Molimo prijavite se.");

    const response = await fetch(BASE_URL, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
    });

    const data = await handleResponse(response);
    return data || [];
};

// =========================================================================
// 2. DOHVAĆANJE VOZILA PO ID-ju (GET by ID)
// =========================================================================
export const fetchVehicleById = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
    });

    const data = await handleResponse(response);
    return data;
};


// =========================================================================
// 3. KREIRANJE NOVOG VOZILA (POST) - Ugrađeno!
// =========================================================================
export const createVehicle = async (vehicleData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(vehicleData)
    });

    return await handleResponse(response);
};


// =========================================================================
// 4. AŽURIRANJE VOZILA (PUT)
// =========================================================================
export const updateVehicle = async (id, vehicleData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(vehicleData)
    });

    return await handleResponse(response);
};

// =========================================================================
// 5. BRISANJE VOZILA (DELETE)
// =========================================================================
export const deleteVehicle = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
    });

    await handleResponse(response);
};

// =========================================================================
// 6. DOHVAĆANJE LISTE VOZAČA (GET)
// =========================================================================
export const fetchDrivers = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const DRIVERS_URL = 'http://localhost:8080/api/users/drivers';

    const response = await fetch(DRIVERS_URL, {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
    });

    const data = await handleResponse(response);

    // Filtriramo listu da zadrži samo valjane objekte s 'id' i 'fullName'
    if (!data || !Array.isArray(data)) return [];

    //return data.filter(driver => driver && driver.id && driver.fullName);
    // Poslije (SonarQube preporuka)
    return data.filter(driver => driver?.id && driver?.fullName);
};

// =========================================================================
// 7. DOHVAĆANJE VOZILA KOJIMA JE SERVIS ISTEKAO (OVERDUE) - Ključno!
// =========================================================================
export const fetchOverdueVehicles = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    // Provjerite da Backend putanja odgovara ovoj! (/api/vehicles/details/overdue)
    const response = await fetch(`${BASE_URL}/details/overdue`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    return await handleResponse(response) || [];
};

// =========================================================================
// 8. DOHVAĆANJE VOZILA KOJA TREBAJU UPOZORENJE (WARNING) - Ključno!
// =========================================================================
export const fetchWarningVehicles = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    // Provjerite da Backend putanja odgovara ovoj! (/api/vehicles/details/warning)
    const response = await fetch(`${BASE_URL}/details/warning`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    return await handleResponse(response) || [];
};

// =========================================================================
// 9. DOHVAĆANJE SLOBODNIH VOZILA (FREE)
// =========================================================================
export const fetchFreeVehiclesDetails = async () => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");
    const response = await fetch(`${BASE_URL}/details/free`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    return await handleResponse(response) || [];
};
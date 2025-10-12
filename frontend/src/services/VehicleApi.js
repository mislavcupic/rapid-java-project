// frontend/src/services/VehicleApi.js (KONAČNA VERZIJA S fetchDrivers)
const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api/vehicles';

// 1. DOHVAĆANJE SVIH VOZILA (GET)
export const fetchVehicles = async () => {
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen. Molimo prijavite se.");
    }

    const response = await fetch(BASE_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        if (response.status === 403) {
            throw new Error("Pristup odbijen. Nemate potrebne ovlasti (ADMIN/MANAGER).");
        }
        throw new Error(`Greška prilikom dohvaćanja vozila: ${response.status} ${response.statusText}`);
    }
    return response.json();
};

// 2. DOHVAĆANJE VOZILA PO ID-ju (GET by ID)
export const fetchVehicleById = async (id) => {
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen.");
    }

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error("Vozilo nije pronađeno.");
        }
        throw new Error(`Greška prilikom dohvaćanja vozila: ${response.status} ${response.statusText}`);
    }
    return response.json();
};

// 3. KREIRANJE VOZILA (POST)
export const createVehicle = async (vehicleData) => {
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen.");
    }

    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(vehicleData)
    });

    if (!response.ok) {
        const errorDetail = await response.json();
        throw new Error(`Kreiranje vozila neuspješno: ${errorDetail.message || response.statusText}`);
    }
    return response.json();
}

// 4. AŽURIRANJE VOZILA (PUT)
export const updateVehicle = async (id, vehicleData) => {
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen.");
    }

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(vehicleData)
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Greška prilikom ažuriranja vozila: ${response.status} ${response.statusText}`);
    }
    return response.json();
};

// 5. BRISANJE VOZILA (DELETE)
export const deleteVehicle = async (id) => {
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen.");
    }

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error(`Greška prilikom brisanja vozila: ${response.status} ${response.statusText}`);
    }
    // DELETE često vraća 204 No Content
};

// 6. DOHVAĆANJE LISTE VOZAČA (GET) - KLJUČNO ZA FRONTEND KOMPONENTE
export const fetchDrivers = async () => { // <--- OVAJ 'export const' JE KLJUČAN
    const token = getToken();
    if (!token) {
        throw new Error("Korisnik nije prijavljen.");
    }

    // URL koji ste potvrdili da postoji u UserController.java
    const DRIVERS_URL = 'http://localhost:8080/api/users/drivers';

    const response = await fetch(DRIVERS_URL, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        if (response.status === 403) {
            throw new Error("Pristup odbijen. Samo ADMIN/MANAGER smiju vidjeti listu vozača.");
        }
        let errorDetail = await response.text();
        try {
            errorDetail = JSON.parse(errorDetail).message;
        } catch (e) {
            // Nije JSON
        }
        throw new Error(`Greška prilikom dohvaćanja vozača: ${response.status} ${errorDetail || response.statusText}`);
    }

    return response.json();
};
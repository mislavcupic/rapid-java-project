// frontend/src/services/AssignmentApi.js

const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api/assignments'; // Koristimo 'assignments' iz Spring Controllera

// Pomoćna funkcija za obradu grešaka
const handleResponse = async (response) => {
    if (!response.ok) {
        let errorDetail = {};
        try {
            // Pokušaj parsirati grešku kao JSON (za ConflictException ili Validation)
            errorDetail = await response.json();
        } catch (err) {
            // Ako nije JSON, koristi tekst ili status
            console.error("Greška pri dohvaćanju dodjele:", err);
            errorDetail.message = response.statusText;
        }
        // Bacamo grešku s detaljnom porukom
        throw new Error(errorDetail.message || `Greška [${response.status}]: ${response.statusText}`);
    }
    // DELETE često vraća 204 No Content
   // return response.status !== 204 ? response.json() : null;
    return response.status === 204 ? null : response.json();
};

// 1. DOHVAĆANJE SVIH DODJELA (GET)
export const fetchAssignments = async () => {
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

// 2. DOHVAĆANJE  PO ID-ju (GET by ID)
export const fetchAssignmentById = async (id) => {
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

// 3. KREIRANJE DODJELE (POST)
export const createAssignment = async (assignmentData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(assignmentData)
    });
    return handleResponse(response);
}

// 4. AŽURIRANJE DODJELE (PUT)
export const updateAssignment = async (id, assignmentData) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(assignmentData)
    });
    return handleResponse(response);
};

// 5. BRISANJE DODJELE (DELETE)
export const deleteAssignment = async (id) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    // DELETE vraća null nakon uspješne provjere
    return handleResponse(response);
};

// 6. DOHVAĆANJE AKTIVNIH DODJELA PO VOZAČU (Dashboard)
export const fetchAssignmentsByDriver = async (driverId) => {
    const token = getToken();
    if (!token) throw new Error("Korisnik nije prijavljen.");

    const response = await fetch(`${BASE_URL}/driver/${driverId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    return handleResponse(response);
};
// frontend/src/services/ShipmentApi.js - S IMPLEMENTIRANIM NOMINATIM GEOCODINGOM

const getToken = () => localStorage.getItem('accessToken');
const BASE_URL = 'http://localhost:8080/api/shipments';

// Pomoćna funkcija za obradu grešaka
const handleResponse = async (response) => {
    if (!response.ok) {
        let errorDetail = {};
        try {
            errorDetail = await response.json();
        } catch (e) {
        log.error(e);
        errorDetail.message = response.statusText;
        throw new Error(errorDetail.message || `Greška [${response.status}]: ${response.statusText}`);
    }
        // Vraća poruku od Backenda ili generičku poruku
        throw new Error(errorDetail.message || `Greška [${response.status}]: ${response.statusText}`);
    }
    return response.status === 204 ?  null : response.json();
};


// FUNKCIJA ZA GEOCORDING PREKO NOMINATIMA (OSM)

/**
 * Pretvara adresu u geografske koordinate (Latitude, Longitude) koristeći Nominatim (OSM).
 * OGRANIČENJE: Koristiti max 1 zahtjev u sekundi.
 * * @param {string} address Adresa za pretraživanje
 * @returns {Promise<{lat: number, lng: number} | null>} Koordinate ili null ako nije pronađeno
 */
export const geocodeAddress = async (address) => {
    if (!address || address.length < 5) return null;

    // Korištenje Public Nominatim API-ja
    const NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

    // Parametri: q=adresa, format=json, limit=1
    const params = new URLSearchParams({
        q: address,
        format: 'json',
        limit: 1,
        'accept-language': 'hr'
    });

    try {
        const response = await fetch(`${NOMINATIM_URL}?${params.toString()}`, {
            // OBAVEZNO: Definiranje User-Agenta za Nominatim (pravilo korištenja)
            headers: {
                'User-Agent': 'RapidLogisticsSystem/1.0 (contact@fleet.io)'
            }
        });

        if (!response.ok) {
            if (response.status === 429) {
                console.error("Nominatim greška: Previše zahtjeva (429). Prekršeno je ograničenje od 1 QPS.");
            }
            throw new Error(`Greška pri geocodingu: ${response.statusText}`);
        }

        const data = await response.json();

        // Nominatim vraća niz rezultata, uzimamo prvi
        if (data && data.length > 0) {
            const result = data[0];
            return {
                lat: Number.parseFloat(result.lat),
                lng: Number.parseFloat(result.lon)
            };
        }

        return null; // Nije pronađena adresa

    } catch (error) {
        console.error("Geocoding Greška:", error.message);
        return null;
    }
};

// =================================================================
// STARE FUNKCIJE (ostaju nepromijenjene, ali su tu radi cjelovitosti)
// =================================================================

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
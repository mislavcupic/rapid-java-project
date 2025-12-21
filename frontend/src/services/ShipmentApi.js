// frontend/src/services/ShipmentApi.js

// ✅ IMPORTIRANJE CENTRALNOG API KLIJENTA
import { apiClient } from './apiClient';

// Relativna putanja do glavnog resursa
const BASE_SHIPMENTS_PATH = '/api/shipments';

// UKLONJENO: const getToken, const BASE_URL, const handleResponse


// =================================================================
// FUNKCIJA ZA GEOCORDING PREKO NOMINATIMA (OSM)
// (OSTAVLJENA NEPROMIJENJENA jer koristi vanjski, javni API)
// =================================================================

/**
 * Pretvara adresu u geografske koordinate (Latitude, Longitude) koristeći Nominatim (OSM).
 * OGRANIČENJE: Koristiti max 1 zahtjev u sekundi.
 * * @param {string} address Adresa za pretraživanje
 * @returns {Promise<{lat: number, lng: number} | null>} Koordinate ili null ako nije pronađeno
 */
export const geocodeAddress = async (address) => {
    if (!address || address.length < 5) return null;

    const NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

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
// SHIPMENT CRUD FUNKCIJE (KORISTE apiClient)
// =================================================================

// 1. DOHVAĆANJE SVIH POŠILJKI (GET)
export const fetchShipments = async () => {
    try {
        const data = await apiClient(BASE_SHIPMENTS_PATH, { method: 'GET' });

        // 🚨 KRITIČNA KOREKCIJA za rješavanje `.map is not a function`
        // Osiguravamo da se za listu uvijek vraća niz.
        return Array.isArray(data) ? data : [];

    } catch (error) {
        console.error("Greška pri dohvaćanju pošiljaka:", error);
        // Proslijedi grešku dalje da bi je komponenta mogla prikazati (Alert)
        throw error;
    }
};

// 2. DOHVAĆANJE POŠILJKE PO ID-ju (GET by ID)
export const fetchShipmentById = async (id) => {
    return apiClient(`${BASE_SHIPMENTS_PATH}/${id}`, { method: 'GET' });
};

// 3. KREIRANJE POŠILJKE (POST)
export const createShipment = async (shipmentData) => {
    return apiClient(BASE_SHIPMENTS_PATH, {
        method: 'POST',
        body: JSON.stringify(shipmentData) // apiClient automatski postavlja Content-Type
    });
}

// 4. AŽURIRANJE POŠILJKE (PUT)
export const updateShipment = async (id, shipmentData) => {
    return apiClient(`${BASE_SHIPMENTS_PATH}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(shipmentData)
    });
};

// 5. BRISANJE POŠILJKE (DELETE)
export const deleteShipment = async (id) => {
    // apiClient automatski rješava 204 No Content odgovor
    return apiClient(`${BASE_SHIPMENTS_PATH}/${id}`, { method: 'DELETE' });
};
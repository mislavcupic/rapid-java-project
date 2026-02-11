// File: src/services/apiClient.js - GLAVNI API INTERCEPTOR

// Osnovni URL backenda
const API_BASE_URL = 'http://localhost:8080';
// Endpoint za osvježavanje tokena (mora biti isti kao u AuthController.java)
const REFRESH_TOKEN_URL = `${API_BASE_URL}/auth/refreshToken`;

/* tu ću namjerno stvoriti konflikt*/
const logoutUser = () => {
    console.error('Refresh token istekao ili opozvan. Automatska odjava.');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userRoles');

    // Preusmjeravanje na stranicu za prijavu
    globalThis.location.href = '/login';

    // Zaustavlja daljnje izvršavanje u pozadini
    throw new Error('Unauthenticated: User logged out.');
};

/**
 * Pokušava osvježiti Access Token koristeći Refresh Token (iz HTTP-Only Cookie-ja)
 * i ponavlja originalni zahtjev s novim Access Tokenom.
 * @param {string} url - Originalni URL zahtjeva (npr. '/api/cars')
 * @param {object} options - Originalne opcije zahtjeva (method, body, itd.)
 * @returns {Promise<any>} Promise koji se rješava konačnim parsiranim JSON objektom
 */
const refreshAndRetry = async (url, options) => {
    try {
        console.log('Token istekao (401). Pokušavam osvježiti token preko Cookie-ja...');

        // 1. POZIV ZA OSVJEŽAVANJE TOKENA
        const refreshResponse = await fetch(REFRESH_TOKEN_URL, {
            method: 'POST',
            credentials: 'include', // KRITIČNO: Uključuje kolačiće u zahtjev
            headers: { 'Content-Type': 'application/json' },
        });

        if (!refreshResponse.ok) {
            // Ako backend vrati 401/403 ovdje, Refresh Token je istekao/nevažeći
            return logoutUser();
        }

        const data = await refreshResponse.json();

        // 2. POHRANA NOVOG ACCESS TOKENA
        localStorage.setItem('accessToken', data.accessToken);

        console.log('Token uspješno osvježen. Ponavljam originalni zahtjev...');

        // 3. PONAVLJANJE ORIGINALNOG ZAHTJEVA S NOVIM TOKENOM
        const newAccessToken = data.accessToken;

        // Ažuriraj Authorization header za ponovljeni zahtjev
        const newOptions = {
            ...options,
            credentials: 'include',
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${newAccessToken}`,
                'Content-Type': 'application/json',
            }
        };

        // Šaljemo zahtjev ponovno
        const retryResponse = await fetch(`${API_BASE_URL}${url}`, newOptions);

        // ✅ KLJUČNA PROMJENA: Parsiraj i vrati JSON objekt
        return processResponse(retryResponse);

    } catch (error) {
        console.error('Greška pri osvježavanju tokena:', error);
        return logoutUser();
    }
};

/**
 * Pomoćna funkcija koja provjerava status odgovora, baca greške za 4xx/5xx i parsira JSON.
 * ✅ KRITIČNO: Ova funkcija popravlja 'response.json is not a function' grešku.
 */
const processResponse = async (response) => {
    // 1. RUKOVANJE GREŠKAMA (Statusi 4xx i 5xx)
    if (!response.ok) {
        let errorMessage = `HTTP greška! Status: ${response.status}`;
        let errorBody = {};

        try {
            // Pokušaj parsirati tijelo greške ako postoji JSON
            errorBody = await response.json();
            errorMessage = errorBody.message || errorBody.error || errorMessage;
        } catch (e) {
            console.error('Greška pri parsiranju error responsa:', e);
            errorMessage = `${response.statusText} (${response.status})`;
        }

        const error = new Error(errorMessage);
        error.status = response.status;
        error.body = errorBody;
        throw error;
    }

    // 2. RUKOVANJE USPJEŠNIM STATUSIMA

    // Rješavanje 204 No Content (često za DELETE metode)
    if (response.status === 204 || response.headers.get('content-length') === '0') {
        return null;
    }

    // 3. Parsiranje standardnog JSON odgovora
    try {
        return await response.json();
    } catch (e) {
        // U slučaju 200 OK, ali praznog tijela (ako nije 204)
        console.warn('Upozorenje: Server je vratio OK status, ali nevažeći JSON odgovor.', e);
        return {};
    }
}


/**
 * Generički API klijent s automatskim dodavanjem Access Tokena i osvježavanjem tokena.
 * @param {string} url - Relativni URL (npr. '/api/users', '/auth/login')
 * @param {object} options - Fetch opcije (method, body, headers, itd.)
 * @returns {Promise<any>} Promise koji se rješava konačnim PARSIRANIM odgovorom ili baca grešku
 */
export const apiClient = async (url, options = {}) => {
    const accessToken = localStorage.getItem('accessToken');

    // 1. Priprema headera
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (accessToken) {
        // Dodaj Access Token u Authorization header
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const requestOptions = {
        ...options,
        headers,
        // KRITIČNO: Omogućuje slanje HTTP-Only Cookie-ja i primanje novih Cookie-ja
        credentials: 'include',
    };

    // 2. Prvi pokušaj slanja zahtjeva
    let response = await fetch(`${API_BASE_URL}${url}`, requestOptions);

    // 3. Provjera 401 (Unauthorized)
    if (response.status === 401 && !url.includes('/auth/login') && !url.includes('/auth/register')) {
        // Ponovno pokušaj nakon osvježavanja tokena
        // refreshAndRetry već vraća parsirani JSON
        return await refreshAndRetry(url, options);
    }


    return processResponse(response);
};
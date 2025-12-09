// frontend/src/services/AdminServiceApi.js

import { apiClient } from './apiClient';

const BASE_ADMIN_PATH = '/api/admin';

// 1. DOHVAĆANJE SVIH KORISNIKA (GET)
export const getAllUsers = async () => {
    try {
        const data = await apiClient(`${BASE_ADMIN_PATH}/users`, { method: 'GET' });
        // ✅ KRITIČNA KOREKCIJA: Osiguraj da je povratna vrijednost uvijek niz.
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju svih korisnika:", error);
        throw error;
    }
};

// 2. AŽURIRANJE ULOGA KORISNIKA (PUT)
export const updateUserRoles = async (userId, roles) => {
    // ✅ ISPRAVAK: Zamotat roles niz u objekt s poljem "roles"
    // Backend očekuje: { "roles": ["ROLE_ADMIN", "ROLE_DRIVER"] }
    return apiClient(`${BASE_ADMIN_PATH}/users/${userId}/roles`, {
        method: 'PUT',
        body: JSON.stringify({ roles: roles })
    });
};

// 3. BRISANJE KORISNIKA (DELETE)
export const deleteUser = async (userId) => {
    return apiClient(`${BASE_ADMIN_PATH}/users/${userId}`, { method: 'DELETE' });
};
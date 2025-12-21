// frontend/src/services/AssignmentApi.js

import { apiClient } from './apiClient';

const BASE_ASSIGNMENTS_PATH = '/api/assignments';

// =================================================================
// ASSIGNMENT CRUD FUNKCIJE
// =================================================================

// 1. DOHVAĆANJE SVIH ASSIGNMENTA (GET)
export const fetchAssignments = async () => {
    try {
        const data = await apiClient(BASE_ASSIGNMENTS_PATH, { method: 'GET' });
        // ✅ KRITIČNA KOREKCIJA: Osiguraj da je povratna vrijednost uvijek niz.
        return Array.isArray(data) ? data : [];
    } catch (error) {
        console.error("Greška pri dohvaćanju assignmenta:", error);
        throw error;
    }
};

// 2. DOHVAĆANJE ASSIGNMENTA PO ID-ju (GET by ID)
export const fetchAssignmentById = async (id) => {
    return apiClient(`${BASE_ASSIGNMENTS_PATH}/${id}`, { method: 'GET' });
};

// 3. KREIRANJE ASSIGNMENTA (POST)
export const createAssignment = async (assignmentData) => {
    return apiClient(BASE_ASSIGNMENTS_PATH, {
        method: 'POST',
        body: JSON.stringify(assignmentData)
    });
}

// 4. AŽURIRANJE ASSIGNMENTA (PUT)
export const updateAssignment = async (id, assignmentData) => {
    return apiClient(`${BASE_ASSIGNMENTS_PATH}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(assignmentData)
    });
};

// 5. BRISANJE ASSIGNMENTA (DELETE)
export const deleteAssignment = async (id) => {
    return apiClient(`${BASE_ASSIGNMENTS_PATH}/${id}`, { method: 'DELETE' });
};
const API_BASE_URL = 'http://localhost:8080';

// Helper funkcija za dohvaćanje tokena
const getAuthHeaders = () => {
    const token = localStorage.getItem('accessToken');
    console.log('Getting auth headers, token exists:', !!token);
    return {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` })
    };
};

// Helper funkcija za obradu odgovora
const handleResponse = async (response) => {
    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('accessToken');
        globalThis.location.href = '/login';
        throw new Error('Neovlašteni pristup');
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    // Ako je 204 No Content, vrati prazan objekt
    if (response.status === 204) {
        return {};
    }

    return response.json();
};

export const getAllUsers = async () => {
    const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
        method: 'GET',
        headers: getAuthHeaders()
    });

    return handleResponse(response);
};

export const updateUserRoles = async (userId, roleNames) => {
    const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}/roles`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ roleNames })
    });

    return handleResponse(response);
};

export const deleteUser = async (userId) => {
    const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
    });

    return handleResponse(response);
};
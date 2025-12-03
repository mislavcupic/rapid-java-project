import { useState, useEffect } from 'react';
import { Container, Row, Col, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import UserTable from './UserTable.jsx';
import EditUserModal from './EditUserModal.jsx';
import { getAllUsers, updateUserRoles, deleteUser } from '../services/AdminServiceApi';

function AdminDashboard() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        // Provjera JWT tokena
        const token = localStorage.getItem('accessToken');
        if (!token) {
            navigate('/login');
            return;
        }

        fetchUsers();
    }, [navigate]);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await getAllUsers();
            setUsers(data);
        } catch (err) {
            // Error handling je već odrađen u handleResponse
            setError(err.message || 'Greška pri učitavanju korisnika');
        } finally {
            setLoading(false);
        }
    };

    const handleEditUser = (user) => {
        setSelectedUser(user);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedUser(null);
    };

    const handleSaveRoles = async (userId, roleNames) => {
        try {
            await updateUserRoles(userId, roleNames);
            await fetchUsers();
            handleCloseModal();
        } catch (err) {
            setError(err.message || 'Greška pri ažuriranju uloga');
        }
    };

    const handleDeleteUser = async (userId) => {
        if (globalThis.confirm('Jeste li sigurni da želite obrisati ovog korisnika?')) {
            try {
                await deleteUser(userId);
                await fetchUsers();
            } catch (err) {
                setError(err.message || 'Greška pri brisanju korisnika');
            }
        }
    };

    if (loading) {
        return (
            <Container className="mt-4">
                <Alert variant="info">Učitavanje korisnika...</Alert>
            </Container>
        );
    }

    return (
        <Container className="mt-4">
            <Row>
                <Col>
                    <h2 className="mb-4">Upravljanje korisnicima</h2>

                    {error && (
                        <Alert variant="danger" dismissible onClose={() => setError(null)}>
                            {error}
                        </Alert>
                    )}

                    <UserTable
                        users={users}
                        onEditUser={handleEditUser}
                        onDeleteUser={handleDeleteUser}
                    />

                    {selectedUser && (
                        <EditUserModal
                            show={showModal}
                            user={selectedUser}
                            onClose={handleCloseModal}
                            onSave={handleSaveRoles}
                        />
                    )}
                </Col>
            </Row>
        </Container>
    );
}

export default AdminDashboard;
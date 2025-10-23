// frontend/src/components/AssignmentList.jsx (KONAČNA VERZIJA)
import React, { useState, useEffect, useCallback } from 'react';
import { fetchAssignments, deleteAssignment } from '../services/AssignmentApi';
import { Table, Alert, Button, Card, Spinner, Modal } from 'react-bootstrap';
import { useNavigate, useLocation } from 'react-router-dom';
import { FaEdit, FaTrash, FaPlus } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const AssignmentList = () => {
    const { t } = useTranslation();
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [assignmentToDelete, setAssignmentToDelete] = useState(null);

    const navigate = useNavigate();
    const location = useLocation();
    const message = location.state?.message;

    // =========================================================================
    // ✅ PROVJERE ULOGA
    // Pretpostavka: Admin i Dispečer smiju raditi sve s dodjelama
    // =========================================================================
    const userRole = localStorage.getItem('userRole');
    const isAdmin = userRole && userRole.includes('ROLE_ADMIN');
    // Svi CRUD-ovi su dostupni Adminu i Dispečeru
    const isDispatcherOrAdmin = isAdmin || (userRole && userRole.includes('ROLE_DISPATCHER'));
    // =========================================================================

    const loadAssignments = useCallback(async () => {
        if (!isAuthenticated) {
            setLoading(false);
            return;
        }
        try {
            const data = await fetchAssignments();
            setAssignments(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        loadAssignments();
        if (message) {
            window.history.replaceState({}, document.title);
        }
    }, [loadAssignments, message]);

    const handleDeleteClick = (assignment) => {
        if (!isDispatcherOrAdmin) {
            setError("Pristup odbijen. Samo ADMINISTRATOR ili DISPEČER smiju brisati dodjele.");
            return;
        }
        setError(null);
        setAssignmentToDelete(assignment);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!assignmentToDelete) return;
        setShowDeleteModal(false);
        try {
            setLoading(true);
            await deleteAssignment(assignmentToDelete.id);
            await loadAssignments();
            navigate('/assignments', { state: { message: `Dodjela ID ${assignmentToDelete.id} uspješno obrisana.` } });
            setAssignmentToDelete(null);
        } catch (err) {
            setError(`Greška pri brisanju: ${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    const handleAddAssignment = () => {
        navigate('/assignments/new');
    };

    if (!isAuthenticated) {
        return (
            <Alert variant="warning" className="text-center shadow font-monospace">
                Molimo, prijavite se za pristup listi dodjela.
            </Alert>
        );
    }

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="info" role="status" />
                <p className="text-muted mt-2">{t("assignments.loading_assignments")}</p>
            </div>
        );
    }

    if (error) {
        return (
            <Alert variant="danger" className="text-center shadow font-monospace">
                Greška: {error}
            </Alert>
        );
    }

    return (
        <>
            <Card className="shadow-lg border-primary border-top-0 border-5">
                <Card.Header className="d-flex justify-content-between align-items-center bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">Popis Dodjela (Assignments)</h1>
                    <Button
                        variant="light"
                        onClick={handleAddAssignment}
                        className="font-monospace fw-bold text-primary"
                        // ✅ GUMB DODAJ: Aktivan za Admina i Dispečera
                        disabled={!isDispatcherOrAdmin}
                        title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju dodavati dodjele" : "Kreiraj novu dodjelu"}
                    >
                        <FaPlus className="me-1" /> Kreiraj Novu Dodjelu
                    </Button>
                </Card.Header>
                <Card.Body>
                    {message && <Alert variant="success" className="font-monospace">{message}</Alert>}

                    {assignments.length === 0 ? (
                        <Alert variant="info" className="text-center font-monospace">
                            Nema registriranih dodjela.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover responsive className="text-center font-monospace">
                                <thead className="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>{t("assignments.status")}</th>
                                    <th>{t("assignments.driver")}</th>
                                    <th>{t("vehicles.vehicle_reg")}</th>
                                    <th>{t("shipments.shipment_tracking")}</th>
                                    <th>Početak</th>
                                    <th className="text-nowrap">{t("general.actions")}</th>
                                </tr>
                                </thead>
                                <tbody>
                                {assignments.map((a) => (
                                    <tr key={a.id}>
                                        <td>{a.id}</td>
                                        <td>{a.assignmentStatus}</td>
                                        <td>{a.driver?.fullName || 'N/A'}</td>
                                        <td>{a.vehicle?.licensePlate || 'N/A'}</td>
                                        <td>{a.shipment?.trackingNumber || 'N/A'}</td>
                                        <td>{new Date(a.startTime).toLocaleString()}</td>

                                        <td className="text-center text-nowrap">
                                            <div className="d-flex justify-content-center">
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    className="me-2 font-monospace fw-bold"
                                                    onClick={() => navigate(`/assignments/edit/${a.id}`)}
                                                    // ✅ GUMB UREDI: Aktivan za Admina i Dispečera
                                                    disabled={!isDispatcherOrAdmin}
                                                    title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju uređivati dodjele" : "Uredi dodjelu"}
                                                >
                                                    <FaEdit className="me-1"/> Uredi
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="font-monospace fw-bold"
                                                    onClick={() => handleDeleteClick(a)}
                                                    // ✅ GUMB IZBRIŠI: Aktivan za Admina i Dispečera
                                                    disabled={!isDispatcherOrAdmin}
                                                    title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju brisati dodjele" : "Izbriši dodjelu"}
                                                >
                                                    <FaTrash className="me-1"/> Izbriši
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            {/* MODAL ZA BRISANJE */}
            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace text-danger">{t("messages.confirm_delete_title")}</Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    Jeste li sigurni da želite izbrisati dodjelu ID **{assignmentToDelete?.id}**?
                    Ova akcija će pošiljku **{assignmentToDelete?.shipment?.trackingNumber}** vratiti u status PENDING.
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-secondary" onClick={() => setShowDeleteModal(false)} className="font-monospace">
                        Odustani
                    </Button>
                    <Button variant="danger" onClick={confirmDelete} className="font-monospace">
                        Izbriši Trajno
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default AssignmentList;
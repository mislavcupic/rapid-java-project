// frontend/src/components/ShipmentList.jsx (KORIGIRANA VERZIJA)

import React, { useState, useEffect, useCallback } from 'react';
import { fetchShipments, deleteShipment } from '../services/ShipmentApi';
import { Table, Alert, Button, Card, Spinner, Modal } from 'react-bootstrap';
import { useNavigate, useLocation } from 'react-router-dom';
// ✅ PROMJENA: Dodan FaEye za Detalje
import { FaEdit, FaTrash, FaPlus, FaEye } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const ShipmentList = () => {
    const { t } = useTranslation();
    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [shipmentToDelete, setShipmentToDelete] = useState(null);

    const navigate = useNavigate();
    const location = useLocation();
    const message = location.state?.message;
    const userRole = localStorage.getItem('userRole');
    const isAdmin = userRole && userRole.includes('ROLE_ADMIN');
    const isDispatcher = userRole && userRole.includes('ROLE_DISPATCHER');

    // Admin i Dispečer smiju kreirati i uređivati (prema ShipmentController.java)
    const canCreate = isAdmin || isDispatcher;
    const canEdit = isAdmin || isDispatcher;
    const canDelete = isAdmin;

    const loadShipments = useCallback(async () => {
        if (!isAuthenticated) {
            setLoading(false);
            return;
        }
        try {
            const data = await fetchShipments();
            setShipments(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        loadShipments();
        if (message) {
            window.history.replaceState({}, document.title);
        }
    }, [loadShipments, message]);

    const handleDeleteClick = (shipment) => {
        if (!canDelete) {
            setError(t("messages.access_denied"));
            return;
        }
        setError(null);
        setShipmentToDelete(shipment);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!shipmentToDelete) return;
        setShowDeleteModal(false);
        try {
            setLoading(true);
            await deleteShipment(shipmentToDelete.id);
            await loadShipments();
            navigate('/shipments', { state: { message: t('messages.shipment_deleted', { trackingNumber: shipmentToDelete.trackingNumber }) } });
            setShipmentToDelete(null);
        } catch (err) {
            setError(t('messages.shipment_delete_error', { error: err.message }));
        } finally {
            setLoading(false);
        }
    };

    const handleAddShipment = () => {
        navigate('/shipments/new');
    };

    if (!isAuthenticated) {
        return (
            <Alert variant="warning" className="text-center shadow font-monospace">
                {t("messages.access_denied")}
            </Alert>
        );
    }

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" role="status" />
                <p className="text-muted mt-2">{t("general.loading")}</p>
            </div>
        );
    }

    if (error) {
        return (
            <Alert variant="danger" className="text-center shadow font-monospace">
                {t("error.general_error")}: {error}
            </Alert>
        );
    }

    return (
        <>
            <Card className="shadow-lg border-primary border-top-0 border-5">
                <Card.Header className="d-flex justify-content-between align-items-center bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">Popis Pošiljki ({t("Shipments")})</h1>
                    <Button
                        variant="light"
                        onClick={handleAddShipment}
                        className="font-monospace fw-bold text-primary"
                        // GUMB DODAJ: Aktivan za Admina i Dispečera
                        disabled={!canCreate}
                        title={!canCreate ? t("messages.access_denied_add_drivers") : t("forms.create_shipment")}
                    >
                        <FaPlus className="me-1" /> {t("forms.create_shipment")}
                    </Button>
                </Card.Header>
                <Card.Body>
                    {message && <Alert variant="success" className="font-monospace">{message}</Alert>}

                    {shipments.length === 0 ? (
                        <Alert variant="info" className="text-center font-monospace">
                            {t("messages.no_data")}
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover responsive className="text-center font-monospace">
                                <thead className="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>{t("shipments.tracking_number")}</th>
                                    <th>{t("assignments.status")}</th>
                                    <th>{t("shipments.origin")}</th>
                                    <th>{t("shipments.destination")}</th>
                                    {/* ✅ PROMJENA: Dodan status rute */}
                                    <th>Status Rute</th>
                                    <th>{t("shipments.description")}</th>
                                    <th className="text-nowrap">{t("general.actions")}</th>
                                </tr>
                                </thead>
                                <tbody>
                                {shipments.map((s) => (
                                    <tr key={s.id}>
                                        <td>{s.id}</td>
                                        <td className="fw-bold">{s.trackingNumber}</td>
                                        <td>{s.status}</td>
                                        <td>{s.originAddress}</td>
                                        <td>{s.destinationAddress}</td>
                                        {/* ✅ PROMJENA: Prikaz statusa rute */}
                                        <td>{s.routeStatus || 'Nije Proračunata'}</td>
                                        <td>{s.description || 'N/A'}</td>

                                        <td className="text-center text-nowrap">
                                            <div className="d-flex justify-content-center">

                                                {/* ✅ NOVO: Gumb Detalji */}
                                                <Button
                                                    variant="outline-info" // Koristimo 'info' boju
                                                    size="sm"
                                                    className="me-2 font-monospace fw-bold"
                                                    onClick={() => navigate(`/shipments/details/${s.id}`)}
                                                    title={t("shipments.details_button") || "Detalji"}
                                                >
                                                    <FaEye className="me-1"/> {t("shipments.details_button") || "Detalji"}
                                                </Button>

                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    className="me-2 font-monospace fw-bold"
                                                    onClick={() => navigate(`/shipments/edit/${s.id}`)}
                                                    // GUMB UREDI: Aktivan za Admina I Dispečera
                                                    disabled={!canEdit}
                                                    title={!canEdit ? t("messages.access_denied_edit_drivers") : t("general.edit")}
                                                >
                                                    <FaEdit className="me-1"/> {t("general.edit")}
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="font-monospace fw-bold"
                                                    onClick={() => handleDeleteClick(s)}
                                                    // GUMB IZBRIŠI: Aktivan SAMO za Admina
                                                    disabled={!canDelete}
                                                    title={!canDelete ? t("messages.access_denied_delete_drivers") : t("general.delete")}
                                                >
                                                    <FaTrash className="me-1"/> {t("general.delete")}
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


            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace text-danger">{t("messages.confirm_delete_title")}</Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    {t("messages.confirm_delete_shipment_text", { trackingNumber: shipmentToDelete?.trackingNumber })}
                    <br/>
                    {t("messages.delete_shipment_condition")}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-secondary" onClick={() => setShowDeleteModal(false)} className="font-monospace">
                        {t("general.cancel")}
                    </Button>
                    <Button variant="danger" onClick={confirmDelete} className="font-monospace">
                        {t("general.delete_permanently")}
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default ShipmentList;
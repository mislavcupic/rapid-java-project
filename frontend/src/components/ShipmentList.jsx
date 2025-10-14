// frontend/src/components/ShipmentList.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { fetchShipments, deleteShipment } from '../services/ShipmentApi';
import { Table, Alert, Button, Card, Spinner, Modal } from 'react-bootstrap';
import { useNavigate, useLocation } from 'react-router-dom';

const ShipmentList = () => {
    const [shipments, setShipments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [shipmentToDelete, setShipmentToDelete] = useState(null);

    const navigate = useNavigate();
    const location = useLocation();
    const message = location.state?.message;

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
            navigate('/shipments', { state: { message: `Pošiljka ${shipmentToDelete.trackingNumber} uspješno obrisana.` } });
            setShipmentToDelete(null);
        } catch (err) {
            setError(`Greška pri brisanju: ${err.message}. Pošiljka se ne smije brisati ako je dodijeljena (SCHEDULED) ili u tranzitu!`);
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
                Molimo, prijavite se za pristup listi pošiljki.
            </Alert>
        );
    }

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" role="status" />
                <p className="text-muted mt-2">Učitavanje pošiljki...</p>
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
                    <h1 className="h4 mb-0 font-monospace">Popis Pošiljki (Shipments)</h1>
                    <Button
                        variant="light"
                        onClick={handleAddShipment}
                        className="font-monospace fw-bold text-primary"
                    >
                        <i className="bi bi-plus-circle me-1"></i> Kreiraj Novu Pošiljku
                    </Button>
                </Card.Header>
                <Card.Body>
                    {message && <Alert variant="success" className="font-monospace">{message}</Alert>}

                    {shipments.length === 0 ? (
                        <Alert variant="info" className="text-center font-monospace">
                            Nema registriranih pošiljki.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover responsive className="text-center font-monospace">
                                <thead className="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Tracking No.</th>
                                    <th>Status</th>
                                    <th>Polazište</th>
                                    <th>Odredište</th>
                                    <th>Opis</th>
                                    <th className="text-nowrap">Akcije</th>
                                </tr>
                                </thead>
                                <tbody>
                                {shipments.map((s) => (
                                    <tr key={s.id}>
                                        <td>{s.id}</td>
                                        <td>**{s.trackingNumber}**</td>
                                        <td>{s.status}</td>
                                        <td>{s.originAddress}</td>
                                        <td>{s.destinationAddress}</td>
                                        <td>{s.description || 'N/A'}</td>

                                        <td className="text-center text-nowrap">
                                            <div className="d-flex justify-content-center">
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    className="me-2 font-monospace fw-bold"
                                                    onClick={() => navigate(`/shipments/edit/${s.id}`)}
                                                >
                                                    Uredi
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="font-monospace fw-bold"
                                                    onClick={() => handleDeleteClick(s)}
                                                >
                                                    Izbriši
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
                    <Modal.Title className="font-monospace text-danger">Potvrda Brisanja</Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    Jeste li sigurni da želite izbrisati pošiljku **{shipmentToDelete?.trackingNumber}**?
                    Brisanje je moguće samo ako je pošiljka u statusu **PENDING**.
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

export default ShipmentList;
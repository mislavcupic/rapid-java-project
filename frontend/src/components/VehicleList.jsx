// frontend/src/components/VehicleList.jsx (KONAČNA ZAŠTIĆENA VERZIJA)
import React, { useState, useEffect, useCallback } from 'react';
import { fetchVehicles, deleteVehicle } from '../services/VehicleApi';
import { Table, Alert, Button, Card, Spinner, Modal } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import { FaEdit, FaTrash, FaPlus } from 'react-icons/fa';


const VehicleList = () => {
    const [vehicles, setVehicles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [vehicleToDelete, setVehicleToDelete] = useState(null);
    const [deleteSuccess, setDeleteSuccess] = useState(null);

    const navigate = useNavigate();

    // =========================================================================
    // ✅ KRITIČNO: DOHVAĆANJE I DEFINIRANJE ULOGA IZ localStorage
    // =========================================================================
    const userRole = localStorage.getItem('userRole');

    const isAdmin = userRole && userRole.includes('ROLE_ADMIN');
    const isDispatcherOrAdmin = isAdmin || (userRole && userRole.includes('ROLE_DISPATCHER'));
    // =========================================================================


    const loadVehicles = useCallback(async () => {
        if (!isAuthenticated) {
            setLoading(false);
            return;
        }
        try {
            const data = await fetchVehicles();
            setVehicles(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        loadVehicles();
    }, [loadVehicles]);

    const handleDeleteClick = (vehicle) => {
        // Front-end zaštita za brisanje (Admin)
        if (!isAdmin) {
            setError("Pristup odbijen. Samo ADMINISTRATOR smije brisati vozila.");
            return;
        }
        setError(null);
        setVehicleToDelete(vehicle);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!vehicleToDelete) return;
        setShowDeleteModal(false);
        try {
            await deleteVehicle(vehicleToDelete.id);
            setVehicles(prev => prev.filter(v => v.id !== vehicleToDelete.id));
            setDeleteSuccess(`Vozilo ${vehicleToDelete.licensePlate} izbrisano.`);
            setTimeout(() => setDeleteSuccess(null), 3000);

        } catch (err) {
            if (err.message && err.message.includes('403')) {
                setError("PRISTUP ODBIJEN (Backend). Samo ADMINISTRATOR smije brisati vozila.");
            } else {
                setError(`Greška pri brisanju: ${err.message}`);
            }
        }
    };

    const handleAddVehicle = () => {
        navigate('/vehicles/add');
    };

    if (!isAuthenticated) {
        return (
            <Alert variant="warning" className="text-center shadow font-monospace">
                Molimo, prijavite se za pristup listi vozila.
            </Alert>
        );
    }

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="info" role="status" />
                <p className="text-muted mt-2">Učitavanje vozila...</p>
            </div>
        );
    }

    return (
        <>
            {error && <Alert variant="danger" className="text-center shadow font-monospace">{error}</Alert>}
            {deleteSuccess && <Alert variant="success" className="text-center shadow font-monospace">{deleteSuccess}</Alert>}

            <Card className="shadow-lg border-info border-top-0 border-5">
                <Card.Header className="d-flex justify-content-between align-items-center bg-info text-white">
                    <h1 className="h4 mb-0 font-monospace">Popis Vozila</h1>
                    <Button
                        variant="light"
                        onClick={handleAddVehicle}
                        className="font-monospace fw-bold text-info"
                        // ✅ Onemogućeno ako nije Admin ili Dispečer
                        disabled={!isDispatcherOrAdmin}
                        title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju dodavati vozila" : "Dodaj novo vozilo"}
                    >
                        <FaPlus className="me-1" /> Dodaj Novo Vozilo
                    </Button>
                </Card.Header>
                <Card.Body>
                    {vehicles.length === 0 ? (
                        <Alert variant="info" className="text-center font-monospace">
                            Nema registriranih vozila.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover responsive className="text-center font-monospace">
                                <thead className="table-dark">
                                <tr>
                                    <th>ID</th><th>Registracija</th><th>Marka / Model</th><th>Godina</th><th>Kapacitet (kg)</th><th>Vozač</th>
                                    <th className="text-nowrap">Akcije</th>
                                </tr>
                                </thead>
                                <tbody>
                                {vehicles.map((v) => (
                                    <tr key={v.id}>
                                        <td>{v.id}</td>
                                        <td>{v.licensePlate}</td>
                                        <td className="text-start">{`${v.make} ${v.model}`}</td>
                                        <td>{v.modelYear || v.year}</td>
                                        <td>{v.loadCapacityKg}</td>
                                        <td>
                                            {v.currentDriver
                                                ? `${v.currentDriver.fullName}`  : <span className="text-danger">Nije dodijeljen</span>}
                                        </td>

                                        <td className="text-center text-nowrap">
                                            <div className="d-flex justify-content-center">
                                                {/* GUMB UREDI */}
                                                <Button
                                                    variant="outline-warning"
                                                    size="sm"
                                                    className="me-2 font-monospace fw-bold"
                                                    onClick={() => navigate(`/vehicles/edit/${v.id}`)}
                                                    // ✅ Onemogućeno ako nije Admin ili Dispečer
                                                    disabled={!isDispatcherOrAdmin}
                                                    title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju uređivati vozila" : "Uredi vozilo"}
                                                >
                                                    <FaEdit className="me-1"/> Uredi
                                                </Button>
                                                {/* GUMB IZBRIŠI */}
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="font-monospace fw-bold"
                                                    onClick={() => handleDeleteClick(v)}
                                                    // ✅ Onemogućeno ako nije Admin
                                                    disabled={!isAdmin}
                                                    title={!isAdmin ? "Samo Admin smije brisati vozila" : "Izbriši vozilo"}
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

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace text-danger">Potvrda Brisanja</Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    Jeste li sigurni da želite izbrisati vozilo s registracijom **{vehicleToDelete?.licensePlate}**?
                    Ova akcija je nepovratna.
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

export default VehicleList;
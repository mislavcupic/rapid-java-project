// frontend/src/components/DriverList.jsx (KONAČNA VERZIJA)
import React, { useState, useEffect, useCallback } from 'react';
import { fetchDrivers, deleteDriver } from '../services/DriverApi';
import { Table, Alert, Button, Card, Spinner, Modal, Container } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import { FaEdit, FaTrash, FaPlus } from 'react-icons/fa';

const DriverList = () => {
    const [drivers, setDrivers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [driverToDelete, setDriverToDelete] = useState(null);
    const [deleteSuccess, setDeleteSuccess] = useState(null);

    const navigate = useNavigate();

    // =========================================================================
    // ✅ PROVJERE ULOGA
    // =========================================================================
    const userRole = localStorage.getItem('userRole');
    const isAdmin = userRole && userRole.includes('ROLE_ADMIN');
    // Uređivanje/Dodavanje smije Admin ili Dispečer
    const isDispatcherOrAdmin = isAdmin || (userRole && userRole.includes('ROLE_DISPATCHER'));
    // =========================================================================

    // Funkcija za dohvaćanje vozača
    const loadDrivers = useCallback(async () => {
        if (!isAuthenticated) {
            setError("Korisnik nije prijavljen.");
            setLoading(false);
            return;
        }
        try {
            const data = await fetchDrivers();
            setDrivers(data);
            setError(null);
        } catch (err) {
            console.error("Greška pri učitavanju vozača:", err);
            setError(err.message || "Greška pri dohvaćanju liste vozača.");
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        loadDrivers();
    }, [loadDrivers]);

    // Rukovanje klikom na gumb 'Izbriši'
    const handleDeleteClick = (driver) => {
        if (!isAdmin) {
            setError("Pristup odbijen. Samo ADMINISTRATOR smije brisati vozače.");
            return;
        }
        setError(null);
        setDriverToDelete(driver);
        setShowDeleteModal(true);
    };

    // Potvrda brisanja
    const confirmDelete = async () => {
        setShowDeleteModal(false);
        if (!driverToDelete) return;

        try {
            await deleteDriver(driverToDelete.id);

            setDeleteSuccess(`Vozač ${driverToDelete.firstName} ${driverToDelete.lastName} je uspješno izbrisan.`);

            setDrivers(prevDrivers => prevDrivers.filter(d => d.id !== driverToDelete.id));
            setDriverToDelete(null);

            setTimeout(() => setDeleteSuccess(null), 3000);

        } catch (err) {
            setError(err.message || "Brisanje nije uspjelo.");
        }
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="success" role="status" />
                <p className="text-muted mt-2">Učitavanje vozača...</p>
            </div>
        );
    }

    if (error && !deleteSuccess) {
        return <Alert variant="danger" className="font-monospace">{error}</Alert>;
    }

    if (!isAuthenticated) {
        return <Alert variant="warning" className="font-monospace">Pristup odbijen. Molimo prijavite se.</Alert>;
    }

    return (
        <Container fluid>
            <Card className="shadow-lg border-success border-top-0 border-5">
                <Card.Header className="bg-success text-white d-flex justify-content-between align-items-center">
                    <h1 className="h4 mb-0 font-monospace">Popis Vozača</h1>
                    <Button
                        as={Link}
                        to="/drivers/add"
                        variant="outline-light"
                        className="fw-bold font-monospace"
                        size="sm"
                        // ✅ GUMB DODAJ: Aktivan za Admina i Dispečera
                        disabled={!isDispatcherOrAdmin}
                        title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju dodavati vozače" : "Dodaj vozača"}
                    >
                        <FaPlus className="me-2" />
                        Dodaj Vozača
                    </Button>
                </Card.Header>
                <Card.Body>
                    {deleteSuccess && <Alert variant="success" className="font-monospace">{deleteSuccess}</Alert>}

                    {drivers.length === 0 ? (
                        <Alert variant="info" className="font-monospace">
                            Trenutno nema registriranih vozača.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover className="text-center align-middle font-monospace">
                                <thead className="table-light">
                                <tr>
                                    <th>ID</th>
                                    <th>Ime i Prezime</th>
                                    <th>OIB</th>
                                    <th>Licenca</th>
                                    <th>Telefon</th>
                                    <th>Akcije</th>
                                </tr>
                                </thead>
                                <tbody>
                                {drivers.map((driver) => (
                                    <tr key={driver.id}>
                                        <td>{driver.id}</td>
                                        <td>{driver.firstName} {driver.lastName}</td>
                                        <td>{driver.oib}</td>
                                        <td>{driver.licenseNumber}</td>
                                        <td>{driver.phoneNumber || 'N/A'}</td>
                                        <td className="text-nowrap">
                                            <div className="d-grid gap-2 d-md-flex justify-content-md-center">
                                                <Button
                                                    as={Link}
                                                    to={`/drivers/edit/${driver.id}`}
                                                    variant="outline-primary"
                                                    size="sm"
                                                    title={!isDispatcherOrAdmin ? "Samo Dispečeri/Admini smiju uređivati vozače" : "Uredi"}
                                                    // ✅ GUMB UREDI: Aktivan za Admina i Dispečera
                                                    disabled={!isDispatcherOrAdmin}
                                                >
                                                    <FaEdit />
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => handleDeleteClick(driver)}
                                                    title={!isAdmin ? "Samo Admin smije brisati vozače" : "Izbriši"}
                                                    // ✅ GUMB IZBRIŠI: Aktivan SAMO za Admina
                                                    disabled={!isAdmin}
                                                >
                                                    <FaTrash />
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

            {/* MODAL za potvrdu brisanja */}
            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace text-danger">Potvrda Brisanja</Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    Jeste li sigurni da želite izbrisati vozača **{driverToDelete?.firstName} {driverToDelete?.lastName}**?
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
        </Container>
    );
};

export default DriverList;
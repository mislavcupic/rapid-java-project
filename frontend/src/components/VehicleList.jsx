// frontend/src/components/VehicleList.jsx (Konačna verzija)
import React, { useState, useEffect, useCallback } from 'react';
import { fetchVehicles, deleteVehicle } from '../services/VehicleApi';
import { Table, Alert, Button, Card, Spinner, Modal, Container } from 'react-bootstrap';
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

    const userRole = localStorage.getItem('userRole');
    const isAdmin = userRole && userRole.includes('ROLE_ADMIN');
    const isDispatcherOrAdmin = isAdmin || (userRole && userRole.includes('ROLE_DISPATCHER'));

    const loadVehicles = useCallback(async () => {
        if (!isAuthenticated) return;
        setLoading(true);
        try {
            // fetchVehicles vraća List<VehicleResponse>
            const data = await fetchVehicles();
            setVehicles(data);
            setError(null);
        } catch (err) {
            setError(err.message || 'Greška pri dohvaćanju vozila.');
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    useEffect(() => {
        loadVehicles();
    }, [loadVehicles]);

    const handleDeleteClick = (vehicle) => {
        setVehicleToDelete(vehicle);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!vehicleToDelete || !isAdmin) {
            setShowDeleteModal(false);
            return;
        }

        try {
            await deleteVehicle(vehicleToDelete.id);
            setDeleteSuccess(`Vozilo ${vehicleToDelete.licensePlate} uspješno izbrisano.`);
            loadVehicles();
        } catch (err) {
            setDeleteSuccess(`ERROR: ${err.message}`);
        } finally {
            setShowDeleteModal(false);
            setVehicleToDelete(null);
        }
    };

    // Pomoćna funkcija za stil statusa
    const getStatusVariant = (remainingKm) => {
        if (remainingKm < 0) {
            return 'text-danger fw-bold'; // OVERDUE
        } else if (remainingKm < 5000) { // Prag upozorenja (npr. 5000 km)
            return 'text-warning fw-bold'; // WARNING
        }
        return 'text-success'; // OK
    };

    // Pomoćna funkcija za tekst statusa
    const getStatusText = (remainingKm) => {
        if (remainingKm < 0) {
            return `Prekoračeno! (${remainingKm * -1} km)`;
        } else if (remainingKm <= 5000) {
            return `${remainingKm.toLocaleString('hr-HR')} km do servisa`;
        }
        return `OK (${remainingKm.toLocaleString('hr-HR')} km)`;
    };


    return (
        <>
            <Container className='mt-5'>
                <Card className='shadow-lg font-monospace'>
                    <Card.Header className='d-flex justify-content-between align-items-center bg-info text-white'>
                        <h4 className='mb-0'>Vozni Park</h4>
                        {isDispatcherOrAdmin && (
                            <Link to="/vehicles/add">
                                <Button variant="light" className="fw-bold">
                                    <FaPlus className="me-1" /> Dodaj Vozilo
                                </Button>
                            </Link>
                        )}
                    </Card.Header>
                    <Card.Body>
                        {deleteSuccess && <Alert variant={deleteSuccess.includes('ERROR') ? 'danger' : 'success'}>{deleteSuccess}</Alert>}
                        {error && <Alert variant="danger">{error}</Alert>}

                        {loading ? (
                            <div className="text-center"><Spinner animation="border" /> Učitavanje...</div>
                        ) : (
                            <div className="table-responsive">
                                <Table striped bordered hover responsive className="mt-4 font-monospace">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Reg. Oznaka</th>
                                        <th>Vozilo (God.)</th>
                                        <th>Nosivost</th>
                                        <th>Trenutni Vozač</th>
                                        <th>Kilometraža</th>
                                        {/* ✅ VRAĆENI STUPCI ZA SERVIS */}
                                        <th>Do Servisa</th>
                                        <th>Status Servisa</th>
                                        <th>Akcije</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {vehicles.map((vehicle) => {
                                        // Pomoćna varijabla za remainingKmToService
                                        const remainingKm = vehicle.remainingKmToService || 0;

                                        return (
                                            <tr key={vehicle.id}>
                                                <td>{vehicle.id}</td>
                                                <td className='fw-bold'>{vehicle.licensePlate}</td>
                                                <td>{vehicle.make} {vehicle.model} ({vehicle.modelYear})</td>
                                                <td>{vehicle.loadCapacityKg?.toLocaleString('hr-HR') || 'N/A'} kg</td>

                                                {/* ISPRAVLJENO: Prikaz vozača uz sigurnu provjeru */}
                                                <td className={vehicle.currentDriver ? 'text-success' : 'text-danger'}>
                                                    {vehicle.currentDriver?.fullName || 'SLOBODAN'}
                                                </td>

                                                <td>{vehicle.currentMileageKm?.toLocaleString('hr-HR') || 0} km</td>

                                                {/* ✅ VRAĆENI PRIKAZ PREOSTALIH KM DO SERVISA */}
                                                <td className={getStatusVariant(remainingKm)}>
                                                    {remainingKm.toLocaleString('hr-HR')} km
                                                </td>

                                                {/* ✅ VRAĆENI PRIKAZ STATUSA SERVISA */}
                                                <td className={getStatusVariant(remainingKm)}>
                                                    {getStatusText(remainingKm)}
                                                </td>

                                                <td>
                                                    <div className="d-flex gap-2">
                                                        {isDispatcherOrAdmin && (
                                                            <Link to={`/vehicles/edit/${vehicle.id}`}>
                                                                <Button variant="outline-primary" size="sm">
                                                                    <FaEdit className="me-1"/> Uredi
                                                                </Button>
                                                            </Link>
                                                        )}
                                                        {isAdmin && (
                                                            <Button variant="outline-danger" size="sm" onClick={() => handleDeleteClick(vehicle)}>
                                                                <FaTrash className="me-1"/> Izbriši
                                                            </Button>
                                                        )}
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })}
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
            </Container>

        </>
    );
};

export default VehicleList;
// frontend/src/components/EditShipment.jsx

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

// ✅ ISPRAVAK: Uvoz shipment funkcija iz ShipmentApi.js
import { updateShipment, fetchShipmentById } from '../services/ShipmentApi';

// ✅ ISPRAVAK: Uvoz fetchDrivers i fetchVehicles iz VehicleApi.js
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';

const EditShipment = () => {
    const { id } = useParams(); // ID pošiljke koju uređujemo
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true); // Za učitavanje postojećih podataka
    const [saving, setSaving] = useState(false); // Za spremanje promjena
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [drivers, setDrivers] = useState([]); // Lista dostupnih vozača
    const [vehicles, setVehicles] = useState([]); // Lista dostupnih vozila

    // Polja DTO-u za pošiljku (ShipmentRequest)
    const [formData, setFormData] = useState({
        originAddress: '',
        destinationAddress: '',
        status: '',
        weightKg: '',
        // KRITIČNO: Dodana polja za Driver i Vehicle ID
        assignedDriverId: '',
        assignedVehicleId: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        // Specijalno rukovanje brojevima (npr. weightKg), ako je potrebno
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Učitavanje liste vozača, vozila i podataka o pošiljci pri renderu
    useEffect(() => {
        const loadData = async () => {
            if (!localStorage.getItem('accessToken')) {
                setLoading(false);
                return navigate('/login');
            }
            try {
                // 1. Dohvaćanje listi za Dropdown (Sada rade jer je uvoz ispravan)
                const driverList = await fetchDrivers();
                setDrivers(driverList);
                const vehicleList = await fetchVehicles();
                setVehicles(vehicleList);

                // 2. Dohvaćanje postojećih podataka pošiljke
                const shipmentData = await fetchShipmentById(id);

                // KRITIČNO: Mapiranje polja iz ShipmetResponse DTO-a u formData
                setFormData({
                    originAddress: shipmentData.originAddress,
                    destinationAddress: shipmentData.destinationAddress,
                    status: shipmentData.status,
                    weightKg: shipmentData.weightKg,
                    // Mapiranje ID-jeva (Ako su null/undefined u backend respons-u, koristimo prazan string)
                    assignedDriverId: shipmentData.assignedDriverId || '',
                    assignedVehicleId: shipmentData.assignedVehicleId || '',
                });

                setLoading(false);
            } catch (err) {
                console.error("Greška pri učitavanju podataka:", err);
                // Provjera je li greška 403 (pristup odbijen)
                setError(err.message || "Greška pri učitavanju podataka pošiljke. Provjerite ovlasti.");
                setLoading(false);
            }
        };
        loadData();
    }, [id, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        try {
            await updateShipment(id, formData);
            setSuccess('Pošiljka uspješno ažurirana!');
            setTimeout(() => navigate('/shipments'), 1500);

        } catch (err) {
            console.error("Greška pri spremanju:", err);
            setError(err.message || 'Greška pri ažuriranju pošiljke.');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <Spinner animation="border" variant="info" role="status" className="d-block mx-auto mt-5" />;
    }

    return (
        <Container style={{ maxWidth: '700px' }}>
            <Card className="shadow-lg border-info border-top-0 border-5 p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">Uređivanje Pošiljke #{id}</h2>
                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit} className="mt-4">

                        {/* 1. Opća polja pošiljke */}
                        <div className="row g-3 mb-4">
                            <div className="col-md-6">
                                <FloatingLabel controlId="originAddress" label="Adresa polaska">
                                    <Form.Control type="text" name="originAddress" value={formData.originAddress} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="destinationAddress" label="Adresa odredišta">
                                    <Form.Control type="text" name="destinationAddress" value={formData.destinationAddress} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="weightKg" label="Težina (kg)">
                                    <Form.Control type="number" name="weightKg" value={formData.weightKg} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="status" label="Status">
                                    <Form.Select name="status" value={formData.status} onChange={handleChange} required className="font-monospace">
                                        <option value="">Odaberite status</option>
                                        <option value="PENDING">PENDING</option>
                                        <option value="IN_TRANSIT">IN_TRANSIT</option>
                                        <option value="DELIVERED">DELIVERED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 2. DODJELA VOZAČA I VOZILA */}
                        <hr className="my-4"/>
                        <h5 className="text-muted font-monospace">Dodjela</h5>
                        <div className="row g-3 mb-4">
                            <div className="col-md-6">
                                <FloatingLabel controlId="assignedDriverId" label="Dodijeljeni Vozač">
                                    <Form.Select name="assignedDriverId" value={formData.assignedDriverId} onChange={handleChange} className="font-monospace">
                                        <option value="">Nije dodijeljen</option>
                                        {drivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {driver.firstName} {driver.lastName} ({driver.email})
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="assignedVehicleId" label="Dodijeljeno Vozilo">
                                    <Form.Select name="assignedVehicleId" value={formData.assignedVehicleId} onChange={handleChange} className="font-monospace">
                                        <option value="">Nije dodijeljeno</option>
                                        {vehicles.map(vehicle => (
                                            <option key={vehicle.id} value={vehicle.id}>
                                                {vehicle.make} {vehicle.model} - {vehicle.licensePlate}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                        </div>


                        <Button
                            type="submit"
                            variant="outline-success"
                            className="w-100 fw-bold font-monospace"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                'Spremi Promjene'
                            )}
                        </Button>
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/shipments')}
                        >
                            Odustani
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditShipment;
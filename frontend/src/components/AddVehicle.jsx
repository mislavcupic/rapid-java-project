// frontend/src/components/AddVehicle.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
// Uvezi funckije za kreiranje vozila i za dohvaćanje vozača
import { createVehicle, fetchDrivers } from '../services/VehicleApi';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

const AddVehicle = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [drivers, setDrivers] = useState([]); // Stanje za listu dostupnih vozača

    const [formData, setFormData] = useState({
        licensePlate: '',
        make: '',
        model: '',
        modelYear: '', // DTO usklađeno polje
        loadCapacityKg: '',
        currentDriverId: '', // Ključno: za dodjelu vozača (šalje se ID)
    });

    // Učitavanje liste vozača pri renderu
    useEffect(() => {
        const loadDrivers = async () => {
            try {
                const driverList = await fetchDrivers();
                setDrivers(driverList);
            } catch (err) {
                console.error("Greška pri učitavanju vozača:", err);
            }
        };
        loadDrivers();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        const vehicleData = {
            licensePlate: formData.licensePlate,
            make: formData.make,
            model: formData.model,
            modelYear: parseInt(formData.modelYear, 10),
            loadCapacityKg: parseInt(formData.loadCapacityKg, 10),
            currentDriverId: formData.currentDriverId ? parseInt(formData.currentDriverId, 10) : null,
        };

        try {
            await createVehicle(vehicleData);
            setSuccess('Vozilo uspješno dodano u flotu!');
            setFormData({ licensePlate: '', make: '', model: '', modelYear: '', loadCapacityKg: '', currentDriverId: '' });

            setTimeout(() => {
                navigate('/vehicles');
            }, 2000);

        } catch (err) {
            const errorMessage = err.message.includes("403") ? "Nemate ovlasti za dodavanje vozila." : err.message;
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    if (!localStorage.getItem('accessToken')) {
        return (
            <Alert variant="warning" className="text-center shadow font-monospace">
                Molimo, prijavite se za dodavanje novih vozila.
            </Alert>
        );
    }

    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-info border-top-0 border-5" style={{ maxWidth: '700px' }}>
                <Card.Header className="bg-info text-white">
                    <h1 className="h4 mb-0 font-monospace">Dodaj Novo Vozilo</h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit}>

                        {/* RED 1: Registracija i Marka */}
                        <div className="row mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="licensePlate" label="Registracija (npr. ZG1234AB)">
                                    <Form.Control type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="make" label="Marka (Proizvođač)">
                                    <Form.Control type="text" name="make" value={formData.make} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* RED 2: Model i Godina */}
                        <div className="row mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="model" label="Model">
                                    <Form.Control type="text" name="model" value={formData.model} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="modelYear" label="Godina Proizvodnje">
                                    <Form.Control type="number" name="modelYear" value={formData.modelYear} onChange={handleChange} required min="1900" max={new Date().getFullYear()} className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* RED 3: Vozač i Nosivost */}
                        <div className="row mb-4">
                            <div className="col-md-6">
                                {/* PADJUĆI IZBORNIK ZA VOZAČA */}
                                <FloatingLabel controlId="currentDriverId" label="Dodijeli Vozača">
                                    <Form.Select
                                        name="currentDriverId"
                                        value={formData.currentDriverId}
                                        onChange={handleChange}
                                        className="font-monospace"
                                    >
                                        <option value="">-- Nije dodijeljen --</option>
                                        {/* Mapiranje liste vozača */}
                                        {drivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {/* Koristimo fullName iz DriverResponseDTO */}
                                                {driver.fullName || 'N/A'}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="loadCapacityKg" label="Nosivost (kg)">
                                    <Form.Control type="number" name="loadCapacityKg" value={formData.loadCapacityKg} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        <Button
                            type="submit"
                            variant="outline-info"
                            className="w-100 fw-bold font-monospace"
                            disabled={loading}
                        >
                            {loading ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                'Spremi Vozilo'
                            )}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddVehicle;
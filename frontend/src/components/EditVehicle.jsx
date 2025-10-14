// frontend/src/components/EditVehicle.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
// Uvezite funkcije za dohvaćanje vozača i ažuriranje vozila
import { updateVehicle, fetchVehicleById, fetchDrivers } from '../services/VehicleApi';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

const EditVehicle = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [drivers, setDrivers] = useState([]); // 1. NOVO STANJE: Lista vozača

    // DTO ISPRAVAK: Dodajemo polje za ID vozača
    const [formData, setFormData] = useState({
        licensePlate: '',
        make: '',
        model: '',
        modelYear: '',
        loadCapacityKg: '',
        currentDriverId: '', // 1. NOVO POLJE: ID trenutnog vozača (šalje se Backendu)
    });

    // Učitavanje liste vozača i podataka o vozilu pri renderu
    useEffect(() => {
        const loadData = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError("Molimo, prijavite se za uređivanje vozila.");
                setLoading(false);
                return;
            }
            try {
                // 2a. Dohvati listu vozača (kao u AddVehicle.jsx)
                const driverList = await fetchDrivers();
                setDrivers(driverList);

                // 2b. Dohvati podatke o vozilu
                const data = await fetchVehicleById(id);

                setFormData({
                    licensePlate: data.licensePlate || '',
                    make: data.make || '',
                    model: data.model || '',
                    modelYear: data.modelYear ? String(data.modelYear) : (data.year ? String(data.year) : ''),
                    loadCapacityKg: data.loadCapacityKg ? String(data.loadCapacityKg) : '',

                    // 2c. KRITIČNO: Postavi ID trenutnog vozača
                    // data.currentDriver je ugniježđeni objekt (nakon ispravke Backenda)
                    currentDriverId: data.currentDriver ? String(data.currentDriver.id) : '',
                });
                setLoading(false);
            } catch (err) {
                console.error("Greška pri učitavanju podataka:", err);
                const errorMessage = err.message.includes("403") ? "Nemate ovlasti za uređivanje vozila." : err.message;
                setError(errorMessage);
                setLoading(false);
            }
        };

        loadData();
    }, [id]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        const vehicleData = {
            licensePlate: formData.licensePlate,
            make: formData.make,
            model: formData.model,
            modelYear: parseInt(formData.modelYear, 10),
            loadCapacityKg: parseInt(formData.loadCapacityKg, 10),

            // KRITIČNO: Šaljemo ID vozača natrag
            currentDriverId: formData.currentDriverId ? parseInt(formData.currentDriverId, 10) : null,
        };

        try {
            await updateVehicle(id, vehicleData);
            setSuccess('Vozilo uspješno ažurirano!');

            setTimeout(() => {
                navigate('/vehicles');
            }, 1500);

        } catch (err) {
            const errorMessage = err.message.includes("403") ? "Nemate ovlasti za ažuriranje vozila." : err.message;
            setError(errorMessage);
        } finally {
            setSaving(false);
        }
    };

    // ... (ostatak koda za loading i error poruke) ...

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="info" role="status" />
                <p className="text-muted mt-2">Učitavanje podataka o vozilu...</p>
            </div>
        );
    }

    // Provjera autentikacije
    if (error && error.includes("prijavite se")) {
        return (
            <Alert variant="warning" className="text-center shadow font-monospace">
                Molimo, prijavite se za uređivanje vozila.
            </Alert>
        );
    }


    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-info border-top-0 border-5" style={{ maxWidth: '700px' }}>
                <Card.Header className="bg-info text-white">
                    <h1 className="h4 mb-0 font-monospace">Uredi Vozilo (ID: {id})</h1>
                </Card.Header>
                <Card.Body>

                    {error && !error.includes("prijavite se") && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit}>
                        {/* ... (Registracija i Marka) ... */}
                        <div className="row mb-3">
                            <div className="col-md-6"><FloatingLabel controlId="licensePlate" label="Registracija"><Form.Control type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                            <div className="col-md-6"><FloatingLabel controlId="make" label="Marka"><Form.Control type="text" name="make" value={formData.make} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                        </div>
                        {/* ... (Model i Godina Proizvodnje) ... */}
                        <div className="row mb-3">
                            <div className="col-md-6"><FloatingLabel controlId="model" label="Model"><Form.Control type="text" name="model" value={formData.model} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="modelYear" label="Godina Proizvodnje">
                                    <Form.Control
                                        type="number"
                                        name="modelYear"
                                        value={formData.modelYear}
                                        onChange={handleChange}
                                        required
                                        min="1900"
                                        max={new Date().getFullYear()}
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 3. KRITIČNO: DODAN PADJUĆI IZBORNIK ZA VOZAČA */}
                        <div className="row mb-4">
                            <div className="col-md-6">
                                <FloatingLabel controlId="currentDriverId" label="Dodijeli Vozača">
                                    <Form.Select
                                        name="currentDriverId"
                                        value={formData.currentDriverId} // Prikazuje trenutno dodijeljenog
                                        onChange={handleChange}
                                        className="font-monospace"
                                    >
                                        <option value="">-- Nije dodijeljen --</option>
                                        {/* Mapiranje liste vozača */}
                                        {drivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {driver.fullName || `${driver.firstName} ${driver.lastName}` || 'N/A'}
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

                        {/* ... (Gumbi za spremanje i odustajanje) ... */}
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
                            onClick={() => navigate('/vehicles')}
                        >
                            Odustani
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditVehicle;
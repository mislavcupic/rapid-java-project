// frontend/src/components/EditVehicle.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { updateVehicle, fetchVehicleById } from '../services/VehicleApi';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

const EditVehicle = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // DTO ISPRAVAK: Koristimo modelYear
    const [formData, setFormData] = useState({
        licensePlate: '',
        make: '',
        model: '',
        modelYear: '', // KRITIČNO
        loadCapacityKg: '',
    });

    useEffect(() => {
        const loadVehicle = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError("Molimo, prijavite se za uređivanje vozila.");
                setLoading(false);
                return;
            }
            try {
                const data = await fetchVehicleById(id);
                setFormData({
                    licensePlate: data.licensePlate || '',
                    make: data.make || '',
                    model: data.model || '',
                    modelYear: data.modelYear ? String(data.modelYear) : '', // KRITIČNO: Dohvaćanje modelYear
                    loadCapacityKg: data.loadCapacityKg ? String(data.loadCapacityKg) : '',
                });
                setLoading(false);
            } catch (err) {
                const errorMessage = err.message.includes("403") ? "Nemate ovlasti za uređivanje vozila." : err.message;
                setError(errorMessage);
                setLoading(false);
            }
        };

        loadVehicle();
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
            modelYear: parseInt(formData.modelYear, 10), // KRITIČNO: Slanje modelYear
            loadCapacityKg: parseInt(formData.loadCapacityKg, 10),
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

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="info" role="status" />
                <p className="text-muted mt-2">Učitavanje podataka o vozilu...</p>
            </div>
        );
    }

    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-info border-top-0 border-5" style={{ maxWidth: '700px' }}>
                <Card.Header className="bg-info text-white">
                    <h1 className="h4 mb-0 font-monospace">Uredi Vozilo (ID: {id})</h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit}>

                        <div className="row mb-3">
                            <div className="col-md-6"><FloatingLabel controlId="licensePlate" label="Registracija"><Form.Control type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                            <div className="col-md-6"><FloatingLabel controlId="make" label="Marka"><Form.Control type="text" name="make" value={formData.make} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                        </div>
                        <div className="row mb-3">
                            <div className="col-md-6"><FloatingLabel controlId="model" label="Model"><Form.Control type="text" name="model" value={formData.model} onChange={handleChange} required className="font-monospace" /></FloatingLabel></div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="modelYear" label="Godina Proizvodnje">
                                    <Form.Control
                                        type="number"
                                        name="modelYear" // KRITIČNO
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
                        <div className="mb-4">
                            <FloatingLabel controlId="loadCapacityKg" label="Nosivost (kg)"><Form.Control type="number" name="loadCapacityKg" value={formData.loadCapacityKg} onChange={handleChange} required min="1" className="font-monospace" /></FloatingLabel>
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
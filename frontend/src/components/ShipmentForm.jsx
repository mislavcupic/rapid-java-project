// frontend/src/components/ShipmentForm.jsx (Create & Edit)

import React, { useState, useEffect } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchShipmentById, createShipment, updateShipment } from '../services/ShipmentApi';


const ShipmentForm = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        description: '',
        status: 'PENDING' // Početni status za novu pošiljku
    });

    const SHIPMENT_STATUSES = ['PENDING', 'SCHEDULED', 'IN_TRANSIT', 'DELIVERED', 'CANCELED'];

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        const loadShipment = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError("Molimo, prijavite se za pristup formi.");
                setLoading(false);
                return;
            }

            if (isEditMode) {
                try {
                    const data = await fetchShipmentById(id);
                    setFormData({
                        trackingNumber: data.trackingNumber || '',
                        originAddress: data.originAddress || '',
                        destinationAddress: data.destinationAddress || '',
                        description: data.description || '',
                        status: data.status || 'PENDING'
                    });
                } catch (err) {
                    console.error("Greška pri učitavanju pošiljke:", err);
                    setError(err.message);
                }
            }
            setLoading(false);
        };

        loadShipment();
    }, [id, isEditMode]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        // ✅ KRITIČNA IZMJENA: ČIŠĆENJE PODATAKA
        const dataToSend = { ...formData };

        // Prođi kroz sva polja i pretvori prazan string u null.
        // OVO JE KRITIČNO za ispravnu Java validaciju (@NotNull).
        for (const key in dataToSend) {
            // Provjeri je li polje prazan string. Ostavite datume i ostale ne-string vrijednosti kakve jesu.
            if (typeof dataToSend[key] === 'string' && dataToSend[key].trim() === '') {
                dataToSend[key] = null;
            }
        }

        // NAPOMENA: Ako su polja 'weight', 'volume', 'value' na backendu obavezni
        // (@NotNull), moraju imati vrijednost.
        // Ako su na backendu obavezni (@NotBlank), onda 'null' NEĆE proći.
        // Ali za numerička polja to rješava problem.

        try {
            if (id) {
                // updateShipment na liniji 72
                await updateShipment(id, dataToSend);
            } else {
                // createShipment na liniji 70
                await createShipment(dataToSend);
            }
            // ... (logika za uspjeh)
        } catch (err) {
            // ... (logika za grešku)
        } finally {
            setLoading(false);
        }
    };
    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" role="status" />
                <p className="text-muted mt-2">Učitavanje forme...</p>
            </div>
        );
    }

    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-primary border-top-0 border-5" style={{ maxWidth: '800px' }}>
                <Card.Header className="bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">{isEditMode ? `Uredi Pošiljku (Tracking: ${formData.trackingNumber})` : 'Kreiraj Novu Pošiljku'}</h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}

                    <Form onSubmit={handleSubmit} className="p-1">

                        <Row className="mb-3">
                            {/* Tracking Number */}
                            <Col md={6}>
                                <FloatingLabel controlId="formTrackingNumber" label="Broj za praćenje (Tracking No.)">
                                    <Form.Control
                                        type="text"
                                        name="trackingNumber"
                                        value={formData.trackingNumber}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* Status */}
                            <Col md={6}>
                                <FloatingLabel controlId="formStatus" label="Status">
                                    <Form.Select
                                        name="status"
                                        value={formData.status}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        // U Edit modu, PENDING se može mijenjati u SCHEDULED ručno, ostalo se obično mijenja kroz Assignment
                                        disabled={isEditMode && formData.status !== 'PENDING' && formData.status !== 'SCHEDULED'}
                                    >
                                        {SHIPMENT_STATUSES.map(s => (
                                            <option key={s} value={s}>{s}</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>

                        <Row className="mb-3">
                            {/* Polazište */}
                            <Col md={6}>
                                <FloatingLabel controlId="formOriginAddress" label="Adresa Polazišta">
                                    <Form.Control
                                        type="text"
                                        name="originAddress"
                                        value={formData.originAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* Odredište */}
                            <Col md={6}>
                                <FloatingLabel controlId="formDestinationAddress" label="Adresa Odredišta">
                                    <Form.Control
                                        type="text"
                                        name="destinationAddress"
                                        value={formData.destinationAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* Opis */}
                        <Row className="mb-4">
                            <Col>
                                <FloatingLabel controlId="formDescription" label="Opis Pošiljke (Opcionalno)">
                                    <Form.Control
                                        as="textarea"
                                        name="description"
                                        value={formData.description}
                                        onChange={handleChange}
                                        className="font-monospace"
                                        style={{ height: '100px' }}
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        <Button
                            type="submit"
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                isEditMode ? 'Spremi Promjene' : 'Kreiraj Pošiljku'
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

export default ShipmentForm;
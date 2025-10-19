// frontend/src/components/ShipmentForm.jsx (Create & Edit)

import React, { useState, useEffect } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchShipmentById, createShipment, updateShipment } from '../services/ShipmentApi';

// Pomoćna funkcija za formatiranje datuma iz Backenda (LocalDateTime) u format YYYY-MM-DDThh:mm
// Ovaj format je obavezan za input type="datetime-local"
const formatDateTimeLocal = (isoString) => {
    if (!isoString) {
        // Ako nema datuma, vraćamo trenutno vrijeme (za novu pošiljku)
        return new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    }
    // Uzimamo samo prvih 16 znakova: YYYY-MM-DDTHH:MM (eliminiramo sekunde i milisekunde)
    return isoString.slice(0, 16);
}

const ShipmentForm = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [success, setSuccess] = useState(null);

    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        description: '',
        status: 'PENDING',

        // ✅ KRITIČNA IZMJENA: Koristimo ispravan format za inicijalizaciju
        expectedDeliveryDate: formatDateTimeLocal(null),
        weightKg: '',
        shipmentValue: '',
        volumeM3: ''
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
                        status: data.status || 'PENDING',

                        // ✅ KRITIČNA IZMJENA: Koristimo formatDateTimeLocal za učitavanje
                        expectedDeliveryDate: formatDateTimeLocal(data.expectedDeliveryDate),

                        weightKg: data.weightKg || '',
                        shipmentValue: data.shipmentValue || '',
                        volumeM3: data.volumeM3 || ''
                    });
                    setError(null);
                } catch (err) {
                    console.error("Greška pri učitavanju pošiljke:", err);
                    setError(err.message || "Greška pri učitavanju pošiljke.");
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
        setSaving(true);
        setError(null);
        setSuccess(null);

        // Provjera obaveznih polja
        if (!formData.trackingNumber || !formData.originAddress || !formData.destinationAddress ||
            !formData.expectedDeliveryDate || !formData.weightKg) {

            setError('Molimo popunite sva obavezna polja (Broj za praćenje, Adrese, Datum i Težina).');
            setSaving(false);
            return;
        }

        // ČIŠĆENJE PODATAKA prije slanja na backend (konverzija "" u null)
        const dataToSend = { ...formData };
        for (const key in dataToSend) {
            const value = dataToSend[key];
            if (typeof value === 'string' && value.trim() === '') {
                dataToSend[key] = null;
            }
        }

        // NAPOMENA: Nije potrebna ručna korekcija datuma/vremena jer ga type="datetime-local" već šalje ispravno.

        try {
            if (id) {
                await updateShipment(id, dataToSend);
                setSuccess('Pošiljka je uspješno ažurirana!');
            } else {
                await createShipment(dataToSend);
                setSuccess('Nova pošiljka je uspješno kreirana!');
            }
            setTimeout(() => navigate('/shipments'), 1500);
        } catch (err) {
            console.error("Greška pri spremanju pošiljke:", err);
            setError(err.message || 'Greška pri spremanju pošiljke. Provjerite podatke i pokušajte ponovno.');
        } finally {
            setSaving(false);
        }
    };

    // UI za Loading
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

                    {/* Prikaz poruka o grešci i uspjehu */}
                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

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

                        {/* ✅ KRITIČNA POLJA: DATUM i TEŽINA */}
                        <Row className="mb-3">
                            {/* Datum Isporuke (Sada uvijek u YYYY-MM-DDThh:mm formatu) */}
                            <Col md={6}>
                                <FloatingLabel controlId="expectedDeliveryDate" label="Očekivani Datum Isporuke">
                                    <Form.Control
                                        type="datetime-local"
                                        name="expectedDeliveryDate"
                                        value={formData.expectedDeliveryDate}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* Težina (kg) */}
                            <Col md={6}>
                                <FloatingLabel controlId="weightKg" label="Težina (kg)">
                                    <Form.Control
                                        type="number"
                                        name="weightKg"
                                        value={formData.weightKg}
                                        onChange={handleChange}
                                        required
                                        min="0.1"
                                        step="0.01"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* Dodatna Numerička polja (Vrijednost i Volumen) */}
                        <Row className="mb-4">
                            {/* Vrijednost Pošiljke */}
                            <Col md={6}>
                                <FloatingLabel controlId="shipmentValue" label="Vrijednost (€)">
                                    <Form.Control
                                        type="number"
                                        name="shipmentValue"
                                        value={formData.shipmentValue}
                                        onChange={handleChange}
                                        min="0"
                                        step="0.01"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* Volumen (m3) */}
                            <Col md={6}>
                                <FloatingLabel controlId="volumeM3" label="Volumen (m³)">
                                    <Form.Control
                                        type="number"
                                        name="volumeM3"
                                        value={formData.volumeM3}
                                        onChange={handleChange}
                                        min="0"
                                        step="0.01"
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
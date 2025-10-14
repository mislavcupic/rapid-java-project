// frontend/src/components/AssignmentForm.jsx (Koristi se i za Create i za Edit)

import React, { useState, useEffect } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchAssignmentById, createAssignment, updateAssignment } from '../services/AssignmentApi';
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi'; // Reupotreba servisa
// Pretpostavljeni API za Pošiljke (MORATE ga implementirati u src/services/ShipmentApi.js)
import { fetchShipments } from '../services/ShipmentApi';


const AssignmentForm = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [formData, setFormData] = useState({
        driverId: '',
        vehicleId: '',
        shipmentId: '',
        startTime: '',
        endTime: '',
        // U edit modu, dohvaćamo status za prikaz (iako se ne šalje u DTO)
        status: 'SCHEDULED'
    });

    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [shipments, setShipments] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // Statusi pošiljke koje dopuštamo za dodjelu (PENDING)
    const ALLOWED_SHIPMENT_STATUSES = ['PENDING'];


    useEffect(() => {
        const loadDependencies = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError("Molimo, prijavite se za pristup formi.");
                setLoading(false);
                return;
            }

            try {
                // 1. Dohvaćanje vanjskih FK podataka (Vozači, Vozila, Pošiljke)
                const [driversRes, vehiclesRes, shipmentsRes] = await Promise.all([
                    fetchDrivers(),
                    fetchVehicles(),
                    fetchShipments() // Pretpostavljena funkcija!
                ]);

                setDrivers(driversRes);
                setVehicles(vehiclesRes);

                let shipmentOptions = shipmentsRes;

                // 2. Dohvaćanje podataka za uređivanje (ako je Edit Mode)
                if (isEditMode) {
                    const data = await fetchAssignmentById(id);

                    // U Edit modu, moramo osigurati da je trenutna pošiljka (koja je SCHEDULED) dostupna u dropdownu
                    if (data.shipment && !shipmentOptions.some(s => s.id === data.shipment.id)) {
                        shipmentOptions.push(data.shipment);
                    }

                    // Postavljanje podataka
                    setFormData({
                        driverId: data.driver.id.toString(),
                        vehicleId: data.vehicle.id.toString(),
                        shipmentId: data.shipment.id.toString(),
                        // Formatiranje za input type="datetime-local"
                        startTime: data.startTime ? data.startTime.substring(0, 16) : '',
                        endTime: data.endTime ? data.endTime.substring(0, 16) : '',
                        status: data.assignmentStatus // Dohvaća status za prikaz
                    });
                }

                // Filtriramo pošiljke: samo PENDING (i trenutno dodijeljena u EDIT modu)
                setShipments(shipmentOptions.filter(s => ALLOWED_SHIPMENT_STATUSES.includes(s.status) || (isEditMode && s.id === formData.shipmentId)));

            } catch (err) {
                console.error("Greška pri učitavanju referentnih podataka:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadDependencies();
    }, [id, isEditMode]); // Dodajemo id i isEditMode u ovisnosti

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);

        // Priprema podataka za DTO (ID-evi moraju biti brojevi, endTime se šalje null ako je prazan)
        const dataToSend = {
            driverId: parseInt(formData.driverId, 10),
            vehicleId: parseInt(formData.vehicleId, 10),
            shipmentId: parseInt(formData.shipmentId, 10),
            startTime: formData.startTime,
            endTime: formData.endTime || null
        };

        try {
            const action = isEditMode
                ? updateAssignment(id, dataToSend)
                : createAssignment(dataToSend);

            await action;

            const successMsg = `Dodjela ID ${id || 'nova'} uspješno spremljena.`;
            navigate('/assignments', { state: { message: successMsg } });
        } catch (err) {
            console.error("Greška pri spremanju dodjele:", err);
            setError(err.message);
        } finally {
            setSaving(false);
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

    if (error && error.includes("prijavite se")) {
        return <Alert variant="warning" className="text-center shadow font-monospace">Molimo, prijavite se.</Alert>;
    }

    // Prikaz forme
    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-primary border-top-0 border-5" style={{ maxWidth: '800px' }}>
                <Card.Header className="bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">{isEditMode ? `Uredi Dodjelu (ID: ${id})` : 'Kreiraj Novu Dodjelu'}</h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}

                    <Form onSubmit={handleSubmit} className="p-1">

                        <Row className="mb-3">
                            {/* Select za Vozača */}
                            <Col md={6}>
                                <FloatingLabel controlId="formDriver" label="Vozač">
                                    <Form.Select name="driverId" value={formData.driverId} onChange={handleChange} required className="font-monospace">
                                        <option value="">Odaberi Vozača...</option>
                                        {drivers.map(d => (
                                            <option key={d.id} value={d.id}>{d.fullName}</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>

                            {/* Select za Vozilo */}
                            <Col md={6}>
                                <FloatingLabel controlId="formVehicle" label="Vozilo">
                                    <Form.Select name="vehicleId" value={formData.vehicleId} onChange={handleChange} required className="font-monospace">
                                        <option value="">Odaberi Vozilo...</option>
                                        {vehicles.map(v => (
                                            <option key={v.id} value={v.id}>{v.licensePlate} ({v.make})</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>

                        <Row className="mb-3">
                            {/* Select za Pošiljku */}
                            <Col md={6}>
                                <FloatingLabel controlId="formShipment" label="Pošiljka (Status: PENDING)">
                                    <Form.Select
                                        name="shipmentId"
                                        value={formData.shipmentId}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        disabled={isEditMode} // Pošiljka je fiksna u Edit modu
                                    >
                                        <option value="">Odaberi Pošiljku...</option>
                                        {shipments.map(s => (
                                            <option key={s.id} value={s.id}>{s.trackingNumber} - ({s.status})</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>

                            {/* Prikaz Statusa (samo u EDIT modu) */}
                            {isEditMode && (
                                <Col md={6}>
                                    <FloatingLabel controlId="formStatus" label="Trenutni Status Dodjele">
                                        <Form.Control type="text" value={formData.status} disabled className="font-monospace fw-bold" />
                                    </FloatingLabel>
                                </Col>
                            )}
                        </Row>

                        <Row className="mb-4">
                            {/* Datum Početka */}
                            <Col md={6}>
                                <FloatingLabel controlId="formStartTime" label="Vrijeme Početka">
                                    <Form.Control type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </Col>

                            {/* Datum Završetka */}
                            <Col md={6}>
                                <FloatingLabel controlId="formEndTime" label="Vrijeme Završetka (Opcionalno)">
                                    <Form.Control type="datetime-local" name="endTime" value={formData.endTime} onChange={handleChange} className="font-monospace" />
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
                                isEditMode ? 'Spremi Promjene' : 'Kreiraj Dodjelu'
                            )}
                        </Button>
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/assignments')}
                        >
                            Odustani
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AssignmentForm;
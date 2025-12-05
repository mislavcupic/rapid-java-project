// frontend/src/components/AddAssignment.jsx

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

//Uvoz Assignment funkcija (za kreiranje)
import { createAssignment } from '../services/AssignmentApi';

// Uvoz Shipment funkcija (za dohvaćanje liste pošiljaka)
import { fetchShipments } from '../services/ShipmentApi';

// Uvoz pomoćnih funkcija (Vozači i Vozila)
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';
import { useTranslation } from 'react-i18next';

const AddAssignment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // Stanja za Dropdown liste
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [shipments, setShipments] = useState([]);

    // DTO (AssignmentRequestDTO) polja - ID-jevi će se slati kao stringovi
    const [formData, setFormData] = useState({
        shipmentId: '',
        driverId: '',
        vehicleId: '',
        assignmentDate: new Date().toISOString().slice(0, 10), // Postavi na današnji datum (YYYY-MM-DD)
        status: 'SCHEDULED' // Default status
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Učitavanje listi (Vozači, Vozila, Pošiljke)
    useEffect(() => {
        const loadDependencies = async () => {
            if (!localStorage.getItem('accessToken')) return navigate('/login');
            try {
                // Dohvat listi iz različitih API servisa
                const driverList = await fetchDrivers();
                setDrivers(driverList);

                const vehicleList = await fetchVehicles();
                setVehicles(vehicleList);

                // Dohvati listu pošiljaka
                const shipmentList = await fetchShipments();
                setShipments(shipmentList);

            } catch (err) {
                console.error("Greška pri učitavanju listi:", err);
                setError(err.message || "Greška pri učitavanju listi za dodjelu.");
            }
        };
        loadDependencies();
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        // Provjera da li su sva ključna polja odabrana
        if (!formData.shipmentId || !formData.driverId || !formData.vehicleId) {
            setError(t('messages.select_all_fields'));
            setLoading(false);
            return;
        }

        try {
            await createAssignment(formData);
            setSuccess('Nova dodjela je uspješno kreirana!');
            setTimeout(() => navigate('/assignments'), 1500);
        } catch (err) {
            console.error("Greška pri kreiranju dodjele:", err);
            setError(err.message || 'Greška pri kreiranju nove dodjele.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container style={{ maxWidth: '700px' }}>
            <Card className="shadow-lg border-info border-top-0 border-5 p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">{t("forms.create_assignment")}</h2>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit} className="mt-4">

                        {/* 1. ODABIR POŠILJKE - (value pretvorena u String) */}
                        <FloatingLabel controlId="shipmentId" label={t("forms.shipment_for_assignment")} className="mb-4">
                            <Form.Select name="shipmentId" value={formData.shipmentId} onChange={handleChange} required className="font-monospace">
                                <option value="">{t("general.select_shipment")}</option>
                                {shipments.map(shipment => (
                                    // ✅ KRITIČNO: Pretvaramo ID u String za ispravnu sinkronizaciju
                                    <option key={shipment.id} value={String(shipment.id)}>
                                        #{shipment.id} - {shipment.originAddress} &rarr; {shipment.destinationAddress}
                                    </option>
                                ))}
                            </Form.Select>
                        </FloatingLabel>

                        <div className="row g-3 mb-4">
                            {/* 2. ODABIR VOZAČA - (value pretvorena u String) */}
                            <div className="col-md-6">
                                <FloatingLabel controlId="driverId" label={t("assignments.driver")}>
                                    <Form.Select name="driverId" value={formData.driverId} onChange={handleChange} required className="font-monospace">
                                        <option value="">{t("general.choose_driver")}</option>
                                        {drivers.map(driver => (
                                            // ✅ KRITIČNO: Pretvaramo ID u String
                                            <option key={driver.id} value={String(driver.id)}>
                                                {driver.firstName} {driver.lastName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>

                            {/* 3. ODABIR VOZILA - (value pretvorena u String) */}
                            <div className="col-md-6">
                                <FloatingLabel controlId="vehicleId" label={t("assignments.vehicle")}>
                                    <Form.Select name="vehicleId" value={formData.vehicleId} onChange={handleChange} required className="font-monospace">
                                        <option value="">{t("general.choose_vehicle")}</option>
                                        {vehicles.map(vehicle => (
                                            // ✅ KRITIČNO: Pretvaramo ID u String
                                            <option key={vehicle.id} value={String(vehicle.id)}>
                                                {vehicle.licensePlate} ({vehicle.make} {vehicle.model})
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 4. DATUM DODJELE */}
                        <FloatingLabel controlId="assignmentDate" label="Datum Dodjele" className="mb-4">
                            <Form.Control type="date" name="assignmentDate" value={formData.assignmentDate} onChange={handleChange} required className="font-monospace" />
                        </FloatingLabel>


                        <Button
                            type="submit"
                            variant="outline-info"
                            className="w-100 fw-bold font-monospace"
                            disabled={loading}
                        >
                            {loading ? (
                                <Spinner as="span" animation="border" size="sm"  aria-hidden="true" className="me-2" />
                            ) : (
                                t("assignments.create_button")
                            )}
                        </Button>
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/assignments')}
                        >
                            {t("general.cancel")}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddAssignment;
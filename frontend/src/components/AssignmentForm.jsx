// frontend/src/components/AssignmentForm.jsx (AŽURIRAN)

import React, { useState, useEffect } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchAssignmentById, createAssignment, updateAssignment } from '../services/AssignmentApi';
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';
import { fetchShipments } from '../services/ShipmentApi';
import { useTranslation } from 'react-i18next';


const ALLOWED_SHIPMENT_STATUSES = new Set(['PENDING']);


const AssignmentForm = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [formData, setFormData] = useState({
        driverId: '',
        vehicleId: '',
        shipmentId: '',
        startTime: '',
        endTime: '',
        status: 'SCHEDULED'
    });

    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [shipments, setShipments] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);


    useEffect(() => {
        const loadDependencies = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError(t("messages.access_denied"));
                setLoading(false);
                return;
            }

            try {
                const [driversRes, vehiclesRes, shipmentsRes] = await Promise.all([
                    fetchDrivers(),
                    fetchVehicles(),
                    fetchShipments()
                ]);

                setDrivers(driversRes);
                setVehicles(vehiclesRes);

                let shipmentOptions = shipmentsRes;

                if (isEditMode) {
                    const data = await fetchAssignmentById(id);

                    if (data.shipment && !shipmentOptions.some(s => s.id === data.shipment.id)) {
                        shipmentOptions.push(data.shipment);
                    }

                    setFormData({
                        driverId: data.driver.id.toString(),
                        vehicleId: data.vehicle.id.toString(),
                        shipmentId: data.shipment.id.toString(),
                        startTime: data.startTime ? data.startTime.substring(0, 16) : '',
                        endTime: data.endTime ? data.endTime.substring(0, 16) : '',
                        status: data.assignmentStatus
                    });
                }

                setShipments(shipmentOptions.filter(s => ALLOWED_SHIPMENT_STATUSES.has(s.status) || (isEditMode && s.id === formData.shipmentId)));

            } catch (err) {
                console.error("Greška pri učitavanju referentnih podataka:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadDependencies();
    }, [id, isEditMode, t]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);

        const dataToSend = {
            driverId: Number.parseInt(formData.driverId, 10),
            vehicleId: Number.parseInt(formData.vehicleId, 10),
            shipmentId: Number.parseInt(formData.shipmentId, 10),
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
                <Spinner animation="border" variant="primary"  />
                <p className="text-muted mt-2">{t("general.loading_form")}</p>
            </div>
        );
    }

    // 🛑 Riješen problem s optional chainingom (iz prethodnog razgovora)
    if (error?.includes("prijavite se")) {
        return <Alert variant="warning" className="text-center shadow font-monospace">{t("messages.access_denied")}</Alert>;
    }

    // 🛑 RJEŠENJE PROBLEMA SGNJEŽĐENOG TERNARNOG IZRAZA (S6717)
    const buttonText = isEditMode
        ? t("assignments.edit_button")
        : t("assignments.create_button");


    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-primary border-top-0 border-5" style={{ maxWidth: '800px' }}>
                <Card.Header className="bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">
                        {isEditMode ? `${t("general.edit")} ${t("Assignments")} (ID: ${id})` : t("assignments.create_button")}
                    </h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}

                    <Form onSubmit={handleSubmit} className="p-1">

                        <Row className="mb-3">
                            <Col md={6}>
                                <FloatingLabel controlId="formDriver" label={t("assignments.driver")}>
                                    <Form.Select name="driverId" value={formData.driverId} onChange={handleChange} required className="font-monospace">
                                        <option value="">{t("general.select_driver")}</option>
                                        {drivers.map(d => (
                                            <option key={d.id} value={d.id}>{d.fullName}</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>

                            <Col md={6}>
                                <FloatingLabel controlId="formVehicle" label={t("assignments.vehicle")}>
                                    <Form.Select name="vehicleId" value={formData.vehicleId} onChange={handleChange} required className="font-monospace">
                                        <option value="">{t("general.select_vehicle")}</option>
                                        {vehicles.map(v => (
                                            <option key={v.id} value={v.id}>{v.licensePlate} ({v.make})</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>

                        <Row className="mb-3">
                            <Col md={6}>
                                <FloatingLabel controlId="formShipment" label={t("assignments.shipment")}>
                                    <Form.Select
                                        name="shipmentId"
                                        value={formData.shipmentId}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        disabled={isEditMode}
                                    >
                                        <option value="">{t("general.select_shipment")}</option>
                                        {shipments.map(s => (
                                            <option key={s.id} value={s.id}>{s.trackingNumber} - ({s.status})</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>

                            {isEditMode && (
                                <Col md={6}>
                                    <FloatingLabel controlId="formStatus" label={t("assignments.status")}>
                                        <Form.Control type="text" value={formData.status} disabled className="font-monospace fw-bold" />
                                    </FloatingLabel>
                                </Col>
                            )}
                        </Row>

                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="formStartTime" label={t("assignments.start_time")}>
                                    <Form.Control type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </Col>

                            <Col md={6}>
                                <FloatingLabel controlId="formEndTime" label={t("assignments.end_time")}>
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
                                // 🛑 Uklonjena 'role="status"' da se riješi S6702 upozorenje
                                <Spinner as="span" animation="border" size="sm" aria-hidden="true" className="me-2" />
                            ) : (
                                buttonText // 🛑 Korištena izdvojena varijabla
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

export default AssignmentForm;
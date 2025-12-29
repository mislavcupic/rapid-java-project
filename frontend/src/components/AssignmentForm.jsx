import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Form, Button, Row, Col, FloatingLabel, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';
import { fetchShipments } from '../services/ShipmentApi';

const AssignmentForm = ({ initialData, onSubmit, saving }) => {
    const { t } = useTranslation();
    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [shipments, setShipments] = useState([]);

    const [formData, setFormData] = useState({
        driverId: initialData?.driverId || '',
        vehicleId: initialData?.vehicleId || '',
        shipmentIds: initialData?.shipmentIds?.map(String) || [],
        startTime: initialData?.startTime?.slice(0, 16) || new Date().toISOString().slice(0, 16)
    });

    useEffect(() => {
        const loadFormData = async () => {
            try {
                const [fetchedDrivers, fetchedVehicles, fetchedShipments] = await Promise.all([
                    fetchDrivers(),
                    fetchVehicles(),
                    fetchShipments()
                ]);
                setDrivers(fetchedDrivers);
                setVehicles(fetchedVehicles);
                setShipments(fetchedShipments);
            } catch (error) {
                console.error("Error loading form data:", error);
            }
        };
        loadFormData();
    }, []);

    const handleSubmit = (e) => {
        e.preventDefault();
        const finalData = {
            driverId: Number.parseInt(formData.driverId, 10),
            vehicleId: Number.parseInt(formData.vehicleId, 10),
            shipmentIds: formData.shipmentIds.map(id => Number.parseInt(id, 10)),
            startTime: formData.startTime,
            status: formData.status || ''
        };
        onSubmit(finalData);
    };

    return (
        <Form onSubmit={handleSubmit}>
            <Row className="g-3 mb-4">
                <Col md={6}>
                    <FloatingLabel label={t("assignments.driver")}>
                        <Form.Select
                            value={formData.driverId}
                            onChange={e => setFormData({...formData, driverId: e.target.value})}
                            required
                        >
                            <option value="">{t("general.select")}</option>
                            {drivers.map(d => (
                                <option key={d.id} value={d.id}>{d.firstName} {d.lastName}</option>
                            ))}
                        </Form.Select>
                    </FloatingLabel>
                </Col>
                <Col md={6}>
                    <FloatingLabel label={t("assignments.vehicle")}>
                        <Form.Select
                            value={formData.vehicleId}
                            onChange={e => setFormData({...formData, vehicleId: e.target.value})}
                            required
                        >
                            <option value="">{t("general.select")}</option>
                            {vehicles.map(v => (
                                <option key={v.id} value={v.id}>{v.licensePlate} ({v.vehicleType})</option>
                            ))}
                        </Form.Select>
                    </FloatingLabel>
                </Col>
            </Row>

            <Form.Group className="mb-4">
                <Form.Label className="fw-bold text-secondary">{t("assignments.start_time")}</Form.Label>
                <Form.Control
                    type="datetime-local"
                    value={formData.startTime}
                    onChange={e => setFormData({...formData, startTime: e.target.value})}
                    required
                />
            </Form.Group>

            <Form.Group className="mb-4">
                <Form.Label className="fw-bold text-secondary">{t("assignments.select_shipments")}</Form.Label>
                <Form.Select
                    multiple
                    className="font-monospace"
                    style={{ height: '200px' }}
                    value={formData.shipmentIds}
                    onChange={e => setFormData({
                        ...formData,
                        shipmentIds: Array.from(e.target.selectedOptions, option => option.value)
                    })}
                    required
                >
                    {shipments.map(s => (
                        <option key={s.id} value={String(s.id)}>📦 #{s.trackingNumber} - {s.destinationAddress}</option>
                    ))}
                </Form.Select>
            </Form.Group>

            <Button type="submit" variant="primary" className="w-100 fw-bold" disabled={saving}>
                {saving && <Spinner size="sm" className="me-2" animation="border" />}
                {t("general.save")}
            </Button>
        </Form>
    );
};

AssignmentForm.propTypes = {
    initialData: PropTypes.object,
    onSubmit: PropTypes.func.isRequired,
    saving: PropTypes.bool
};

export default AssignmentForm;
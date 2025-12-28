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
                // ✅ SONARQUBE FIX: Jasnija imena varijabli u destrukturiranju
                const [fetchedDrivers, fetchedVehicles, fetchedShipments] = await Promise.all([
                    fetchDrivers(),
                    fetchVehicles(),
                    fetchShipments()
                ]);

                setDrivers(fetchedDrivers);
                setVehicles(fetchedVehicles);

                // Filtriramo pošiljke: Prikazujemo PENDING ili one koje su već dodijeljene ovom nalogu
                const currentShipmentIds = initialData?.shipmentIds?.map(String) || [];
                const filteredShipments = fetchedShipments.filter(ship =>
                    ship.status === 'PENDING' || currentShipmentIds.includes(String(ship.id))
                );

                setShipments(filteredShipments);
            } catch (err) {
                // ✅ SONARQUBE FIX: Obavezno logiranje greške
                console.error("Greška pri dohvaćanju podataka za AssignmentForm:", err);
            }
        };
        loadFormData();
    }, [initialData]); // Koristimo initialData kao stabilan okidač

    const handleSubmit = (e) => {
        e.preventDefault();
        // KONVERZIJA U BROJEVE ZA BACKEND
        const finalData = {
            ...formData,
            driverId: Number(formData.driverId),
            vehicleId: Number(formData.vehicleId),
            shipmentIds: formData.shipmentIds.map(Number)
        };
        onSubmit(finalData);
    };

    return (
        <Form onSubmit={handleSubmit} className="p-3">
            <Row className="g-3 mb-3">
                <Col md={6}>
                    <FloatingLabel label={t("general.driver")}>
                        <Form.Select
                            value={formData.driverId}
                            onChange={e => setFormData({...formData, driverId: e.target.value})}
                            required
                        >
                            <option value="">{t("general.choose_driver")}</option>
                            {drivers.map(driver => (
                                <option key={driver.id} value={String(driver.id)}>
                                    {driver.firstName} {driver.lastName}
                                </option>
                            ))}
                        </Form.Select>
                    </FloatingLabel>
                </Col>
                <Col md={6}>
                    <FloatingLabel label={t("general.vehicle")}>
                        <Form.Select
                            value={formData.vehicleId}
                            onChange={e => setFormData({...formData, vehicleId: e.target.value})}
                            required
                        >
                            <option value="">{t("general.choose_vehicle")}</option>
                            {vehicles.map(vehicle => (
                                <option key={vehicle.id} value={String(vehicle.id)}>
                                    {vehicle.licensePlate}
                                </option>
                            ))}
                        </Form.Select>
                    </FloatingLabel>
                </Col>
            </Row>

            <Form.Group className="mb-4">
                <Form.Label className="fw-bold text-primary">
                    {t("forms.select_multiple_shipments")} (CTRL + klik za više rješenja)
                </Form.Label>
                <Form.Select
                    multiple
                    style={{ height: '200px' }}
                    value={formData.shipmentIds}
                    onChange={e => setFormData({
                        ...formData,
                        shipmentIds: Array.from(e.target.selectedOptions, option => option.value)
                    })}
                    required
                >
                    {shipments.map(shipment => (
                        <option key={shipment.id} value={String(shipment.id)}>
                            📦 #{shipment.id} - {shipment.destinationAddress}
                        </option>
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

// ✅ SONARQUBE FIX: Detaljna validacija prop-ova
AssignmentForm.propTypes = {
    initialData: PropTypes.shape({
        driverId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        vehicleId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        shipmentIds: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number])),
        startTime: PropTypes.string
    }),
    onSubmit: PropTypes.func.isRequired,
    saving: PropTypes.bool
};

// Default props u slučaju da initialData nije poslan
AssignmentForm.defaultProps = {
    initialData: null,
    saving: false
};

export default AssignmentForm;
// frontend/src/components/EditShipment.jsx - KRITIČNO ISPRAVLJENO: VRAĆENO POLJE estimatedDeliveryTime
// ✅ SonarCube: Dodana validacija propova, ispravljena nula-razlomak i rukovanje greškama.

import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner, Row, Col } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
// ✅ SonarCube: Dodan PropTypes za validaciju propova
import PropTypes from 'prop-types';

// =================================================================
// 🛑 REACT LEAFLET UVEZ (ostaje isti)
// =================================================================
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import { updateShipment, fetchShipmentById, geocodeAddress } from '../services/ShipmentApi';
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';

// 🛑 KRITIČNO: EKSPLICITAN UVOZ IKONA ZA LEAFLET (ostaje isti)
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';


// [Pomoćne funkcije: customIcon, formatDateTimeLocal]

const customIcon = new L.Icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon2x,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const formatDateTimeLocal = (isoString) => {
    if (!isoString) return '';
    return isoString.slice(0, 16);
}

const debounce = (func, delay) => {
    let timeoutId;
    return (...args) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            // ✅ SonarCube: Korištenje spread operatora umjesto .apply()
            func(...args);
        }, delay);
    };
};

const MapUpdater = ({ origin, destination }) => {
    const map = useMap();

    useEffect(() => {
        const isOriginValid = origin.lat !== 0 || origin.lng !== 0;
        const isDestinationValid = destination.lat !== 0 || destination.lng !== 0;

        if (isOriginValid && isDestinationValid) {
            const bounds = L.latLngBounds([
                [origin.lat, origin.lng],
                [destination.lat, destination.lng]
            ]);
            map.fitBounds(bounds, { padding: [50, 50] });
        } else if (isOriginValid) {
            map.setView([origin.lat, origin.lng], 13);
        } else if (isDestinationValid) {
            map.setView([destination.lat, destination.lng], 13);
        }
    }, [map, origin, destination]);

    return null;
};

// ✅ SonarCube: Dodavanje validacije propova za MapUpdater
MapUpdater.propTypes = {
    origin: PropTypes.shape({
        lat: PropTypes.number.isRequired,
        lng: PropTypes.number.isRequired,
    }).isRequired,
    destination: PropTypes.shape({
        lat: PropTypes.number.isRequired,
        lng: PropTypes.number.isRequired,
    }).isRequired,
};


const EditShipment = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [drivers, setDrivers] = useState([]);
    const [vehicles, setVehicles] = useState([]);
    const [driversLoading, setDriversLoading] = useState(true);
    const [vehiclesLoading, setVehiclesLoading] = useState(true);

    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        status: '',
        // ✅ SonarCube: Promijenjeno 0.0 u 0
        weightKg: 0,
        // ✅ KRITIČNO: VRAĆENO JE POLJE estimatedDeliveryTime
        estimatedDeliveryTime: '',
        description: '',
        // ✅ SonarCube: Promijenjeno 0.0 u 0
        originLatitude: 0,
        originLongitude: 0,
        destinationLatitude: 0,
        destinationLongitude: 0,
        currentDriverId: '',
        currentVehicleId: '',
    });

    // ✅ SonarCube: Promijenjeno 0 u inicijalnom stanju
    const [originCoords, setOriginCoords] = useState({ lat: 0, lng: 0 });
    const [destinationCoords, setDestinationCoords] = useState({ lat: 0, lng: 0 });
    const [geocodeLoading, setGeocodeLoading] = useState(false);


    // [Logika za geocoding i debouncing ostaje ista]
    const debouncedGeocodeOrigin = useCallback(
        debounce(async (address) => {
            if (!address) {
                setOriginCoords({ lat: 0, lng: 0 });
                // ✅ SonarCube: Promijenjeno 0.0 u 0
                setFormData(prev => ({ ...prev, originLatitude: 0, originLongitude: 0 }));
                return;
            }
            setGeocodeLoading(true);
            try {
                const coords = await geocodeAddress(address);
                if (coords) {
                    setOriginCoords(coords);
                    setFormData(prev => ({
                        ...prev,
                        originLatitude: coords.lat,
                        originLongitude: coords.lng
                    }));
                }
            } catch (err) {
                // ✅ SonarCube: Ispravak rukovanja greškom - logiranje za debbugging
                console.error("Geocoding Origin Error:", err);
            } finally {
                setGeocodeLoading(false);
            }
        }, 1000),
        []
    );

    const debouncedGeocodeDestination = useCallback(
        debounce(async (address) => {
            if (!address) {
                setDestinationCoords({ lat: 0, lng: 0 });
                // ✅ SonarCube: Promijenjeno 0.0 u 0
                setFormData(prev => ({ ...prev, destinationLatitude: 0, destinationLongitude: 0 }));
                return;
            }
            setGeocodeLoading(true);
            try {
                const coords = await geocodeAddress(address);
                if (coords) {
                    setDestinationCoords(coords);
                    setFormData(prev => ({
                        ...prev,
                        destinationLatitude: coords.lat,
                        destinationLongitude: coords.lng
                    }));
                }
            } catch (err) {
                // ✅ SonarCube: Ispravak rukovanja greškom - logiranje za debbugging
                console.error("Geocoding Destination Error:", err);
            } finally {
                setGeocodeLoading(false);
            }
        }, 1000),
        []
    );


    useEffect(() => {
        const loadData = async () => {
            try {
                const shipmentData = await fetchShipmentById(id);
                const [driverList, vehicleList] = await Promise.all([
                    fetchDrivers(),
                    fetchVehicles(),
                ]);

                setDrivers(driverList);
                setVehicles(vehicleList);

                setFormData({
                    trackingNumber: shipmentData.trackingNumber || '',
                    originAddress: shipmentData.originAddress || '',
                    destinationAddress: shipmentData.destinationAddress || '',
                    status: shipmentData.status || 'PENDING',
                    weightKg: shipmentData.weightKg || 0,
                    // ✅ KRITIČNO: ISPRAVNO POSTAVLJANJE POSTOJEĆEG DATUMA
                    estimatedDeliveryTime: formatDateTimeLocal(shipmentData.estimatedDeliveryTime),
                    originLatitude: shipmentData.originLatitude || 0,
                    originLongitude: shipmentData.originLongitude || 0,
                    destinationLatitude: shipmentData.destinationLatitude || 0,
                    destinationLongitude: shipmentData.destinationLongitude || 0,
                    description: shipmentData.description || '',
                    currentDriverId: shipmentData.driverId ? String(shipmentData.driverId) : '',
                    currentVehicleId: shipmentData.vehicleId ? String(shipmentData.vehicleId) : '',
                });

                setOriginCoords({ lat: shipmentData.originLatitude || 0, lng: shipmentData.originLongitude || 0 });
                setDestinationCoords({ lat: shipmentData.destinationLatitude || 0, lng: shipmentData.destinationLongitude || 0 });

            } catch (err) {
                console.error(err);
                setError(t('shipments.error_load'));
            } finally {
                setLoading(false);
                setDriversLoading(false);
                setVehiclesLoading(false);
            }
        };

        loadData();
    }, [id, t]);

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));

        if (name === 'originAddress') {
            debouncedGeocodeOrigin(value);
        } else if (name === 'destinationAddress') {
            debouncedGeocodeDestination(value);
        } else if (name === 'currentDriverId' || name === 'currentVehicleId') {
            const newDriverId = name === 'currentDriverId' ? value : formData.currentDriverId;
            const newVehicleId = name === 'currentVehicleId' ? value : formData.currentVehicleId;

            if (newDriverId && newVehicleId && formData.status === 'PENDING') {
                setFormData(prev => ({ ...prev, status: 'ASSIGNED' }));
            } else if (!newDriverId || !newVehicleId) {
                setFormData(prev => ({ ...prev, status: 'PENDING' }));
            }
        }
    };


    const handleSubmit = async (e) => {
        e.preventDefault();

        setError(null);
        setSuccess(null);
        setSaving(true);

        try {
            if ((!formData.originLatitude && formData.originAddress) || (!formData.destinationLatitude && formData.destinationAddress)) {
                setError(t('shipments.address_geocode_warning'));
                setSaving(false);
                return;
            }

            const shipmentData = {
                ...formData,
                weightKg: Number.parseFloat(formData.weightKg),
                originLatitude: Number.parseFloat(formData.originLatitude),
                originLongitude: Number.parseFloat(formData.originLongitude),
                destinationLatitude: Number.parseFloat(formData.destinationLatitude),
                destinationLongitude: Number.parseFloat(formData.destinationLongitude),
                driverId: formData.currentDriverId ? Number.parseInt(formData.currentDriverId, 10) : null,
                vehicleId: formData.currentVehicleId ? Number.parseInt(formData.currentVehicleId, 10) : null,
            };

            delete shipmentData.currentDriverId;
            delete shipmentData.currentVehicleId;


            await updateShipment(id, shipmentData);
            setSuccess(t('shipments.success_edit'));
            navigate('/shipments', { state: { message: t('shipments.success_edit') } });
        } catch (err) {
            setError(err.message || t('shipments.error_general'));
        } finally {
            setSaving(false);
        }
    };


    // =========================================================================
    // RENDERIRANJE KOMPONENTE
    // =========================================================================
    if (loading) {
        return (
            <Container className="my-5 d-flex justify-content-center">
                <Spinner animation="border" variant="info" />
            </Container>
        );
    }

    const initialCenter = (originCoords.lat !== 0 || originCoords.lng !== 0) ? [originCoords.lat, originCoords.lng] :
        [45.815, 15.9819]; // Zagreb

    // ✅ SonarCube: Promijenjeno 10:13 u 10 ili 13
    const initialZoom = (originCoords.lat !== 0 && destinationCoords.lat !== 0) ? 10 : 13;


    return (
        <Container className="my-5">
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace mb-4">
                        {t('shipments.edit_title')}
                    </h2>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    {/* Forma se zatvara prije gumba. */}
                    <Form id="shipment-edit-form" onSubmit={handleSubmit}>
                        {/* 1. BROJ ZA PRAĆENJE i STATUS */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="trackingNumber" label={t('shipments.tracking_label')}>
                                    <Form.Control
                                        type="text"
                                        name="trackingNumber"
                                        value={formData.trackingNumber}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={50}
                                    />
                                </FloatingLabel>
                            </Col>
                            <Col md={6}>
                                <FloatingLabel controlId="status" label={t('shipments.status_label')}>
                                    <Form.Select
                                        name="status"
                                        value={formData.status}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        disabled={formData.status === 'ASSIGNED' || formData.status === 'IN_TRANSIT' || formData.status === 'DELIVERED'}
                                    >
                                        <option value="PENDING">{t('shipments.status_pending')}</option>
                                        <option value="ASSIGNED" disabled>{t('shipments.status_assigned')}</option>
                                        <option value="IN_TRANSIT">{t('shipments.status_in_transit')}</option>
                                        <option value="DELIVERED">{t('shipments.status_delivered')}</option>
                                        <option value="CANCELLED">{t('shipments.status_cancelled')}</option>
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>


                        {/* 2. POLAZIŠTE i ODREDIŠTE */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="originAddress" label={t('shipments.origin_label') + (geocodeLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Control
                                        type="text"
                                        name="originAddress"
                                        value={formData.originAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={100}
                                    />
                                </FloatingLabel>
                                {originCoords.lat !== 0 && (
                                    <Form.Text muted className="ms-2 font-monospace">
                                        Koordinate: {originCoords.lat.toFixed(4)}, {originCoords.lng.toFixed(4)}
                                    </Form.Text>
                                )}
                            </Col>
                            <Col md={6}>
                                <FloatingLabel controlId="destinationAddress" label={t('shipments.destination_label') + (geocodeLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Control
                                        type="text"
                                        name="destinationAddress"
                                        value={formData.destinationAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={100}
                                    />
                                </FloatingLabel>
                                {destinationCoords.lat !== 0 && (
                                    <Form.Text muted className="ms-2 font-monospace">
                                        Koordinate: {destinationCoords.lat.toFixed(4)}, {destinationCoords.lng.toFixed(4)}
                                    </Form.Text>
                                )}
                            </Col>
                        </Row>

                        {/* 3. TEŽINA i VRIJEME DOSTAVE */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="weightKg" label={t('shipments.weight_label')}>
                                    <Form.Control
                                        type="number"
                                        name="weightKg"
                                        value={formData.weightKg}
                                        onChange={handleChange}
                                        required
                                        min="1"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                            {/* ✅ KRITIČNO: VRAĆENO POLJE ZA DATUM */}
                            <Col md={6}>
                                <FloatingLabel controlId="estimatedDeliveryTime" label={t('shipments.delivery_time_label')}>
                                    <Form.Control
                                        type="datetime-local"
                                        name="estimatedDeliveryTime"
                                        value={formData.estimatedDeliveryTime}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* SKRIVENA POLJA ZA KOORDINATE... */}
                        <Form.Control type="hidden" name="originLatitude" value={formData.originLatitude} />
                        <Form.Control type="hidden" name="originLongitude" value={formData.originLongitude} />
                        <Form.Control type="hidden" name="destinationLatitude" value={formData.destinationLatitude} />
                        <Form.Control type="hidden" name="destinationLongitude" value={formData.destinationLongitude} />


                        {/* 5. DODJELA (ASSIGNMENT) - VOZAČ I VOZILO */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="currentDriverId" label={t('assignments.driver_label') + (driversLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Select
                                        name="currentDriverId"
                                        value={formData.currentDriverId}
                                        onChange={handleChange}
                                        className="font-monospace"
                                    >
                                        <option value="">{t('general.select')}</option>
                                        {drivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {driver.firstName} {driver.lastName} - ({driver.userInfo.username})
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                            <Col md={6}>
                                <FloatingLabel controlId="currentVehicleId" label={t('assignments.vehicle_label') + (vehiclesLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Select
                                        name="currentVehicleId"
                                        value={formData.currentVehicleId}
                                        onChange={handleChange}
                                        className="font-monospace"
                                    >
                                        <option value="">{t('general.select')}</option>
                                        {vehicles.map(vehicle => (
                                            <option key={vehicle.id} value={vehicle.id}>
                                                {vehicle.make} {vehicle.model} - {vehicle.licensePlate}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* 7. OPIS (ZADNJE POLJE FORME) */}
                        <Row className="mb-4">
                            <Col>
                                <FloatingLabel controlId="description" label={t('shipments.description_label')}>
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

                    </Form> {/* 🛑 FORMA JE ZATVORENA! */}

                    {/* =================================================================
                    ✅ GUMBI ZA AKCIJU - ODMAH ISPOD ZATVORENOG FORM TAGA
                    ================================================================= */}
                    <Button
                        onClick={handleSubmit}
                        variant="outline-primary"
                        className="w-100 fw-bold font-monospace mt-3"
                        disabled={saving}
                    >
                        {saving ? (
                            <Spinner as="span" animation="border" size="sm"  aria-hidden="true" className="me-2" />
                        ) : (
                            t("general.save_changes")
                        )}
                    </Button>
                    <Button
                        variant="outline-secondary"
                        className="w-100 fw-bold font-monospace mt-2 mb-4"
                        onClick={() => navigate('/shipments')}
                    >
                        {t('general.cancel')}
                    </Button>


                    {/* =================================================================
                    ✅ MAPA (PRIKAZ) - POSLJEDNJI ELEMENT
                    ================================================================= */}
                    <hr className="my-4 border-info" />
                    <div className="p-3 border rounded shadow-sm">
                        <h5 className="text-dark fw-bold font-monospace mb-3">{t('shipments.map_title')}</h5>

                        <MapContainer
                            center={initialCenter}
                            zoom={initialZoom}
                            scrollWheelZoom={false}
                            className="leaflet-container"
                            style={{ height: '400px', width: '100%' }}
                        >
                            <TileLayer
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            />

                            <MapUpdater origin={originCoords} destination={destinationCoords} />

                            {originCoords.lat !== 0 && (
                                <Marker position={originCoords} icon={customIcon}>
                                    <Popup>
                                        **{t("shipments.origin_label")}:** <br/> {formData.originAddress}
                                    </Popup>
                                </Marker>
                            )}

                            {destinationCoords.lat !== 0 && (
                                <Marker position={destinationCoords} icon={customIcon}>
                                    <Popup>
                                        **{t("shipments.destination_label")}:** <br/> {formData.destinationAddress}
                                    </Popup>
                                </Marker>
                            )}

                        </MapContainer>
                    </div>

                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditShipment;
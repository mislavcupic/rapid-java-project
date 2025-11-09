// frontend/src/components/ShipmentForm.jsx - KOMPLETNO RJEŠENJE S DVA DATUMA, LEAFLET MAPOM I GUMBOM ZA DETALJE

import React, { useState, useEffect, useCallback } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchShipmentById, createShipment, updateShipment, geocodeAddress } from '../services/ShipmentApi';
import { useTranslation } from 'react-i18next';

// =================================================================
// 🛑 UVEZI: LEAFLET
// =================================================================
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// 🛑 KRITIČNO: EKSPLICITAN UVOZ IKONA ZA LEAFLET
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';


// =================================================================
// POMOĆNE FUNKCIJE I KOMPONENTE
// =================================================================
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
    if (!isoString) {
        // Postavi na trenutno vrijeme (za novu pošiljku)
        return new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    }
    // API vraća ISO format, moramo ga skratiti za <input type="datetime-local">
    return isoString.slice(0, 16);
}

const debounce = (func, delay) => {
    let timeoutId;
    return (...args) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            func.apply(null, args);
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


const ShipmentForm = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        status: 'PENDING',
        weightKg: 0,
        // ✅ departureTime (Polazak)
        departureTime: formatDateTimeLocal(null),
        // ✅ expectedDeliveryDate (Dolazak - Usklađeno s Java backendom)
        expectedDeliveryDate: formatDateTimeLocal(null),
        description: '',
        originLatitude: 0.0,
        originLongitude: 0.0,
        destinationLatitude: 0.0,
        destinationLongitude: 0.0,
    });

    const [originCoords, setOriginCoords] = useState({ lat: 0, lng: 0 });
    const [destinationCoords, setDestinationCoords] = useState({ lat: 0, lng: 0 });
    const [geocodeLoading, setGeocodeLoading] = useState(false);


    const debouncedGeocodeOrigin = useCallback(
        debounce(async (address) => {
            // ... (Geocoding logika ostaje ista)
            if (!address) {
                setOriginCoords({ lat: 0, lng: 0 });
                setFormData(prev => ({ ...prev, originLatitude: 0.0, originLongitude: 0.0 }));
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
                } else {
                    setOriginCoords({ lat: 0, lng: 0 });
                    setFormData(prev => ({ ...prev, originLatitude: 0.0, originLongitude: 0.0 }));
                }
            } catch (err) {
                // Ignoriraj geocode greške
            } finally {
                setGeocodeLoading(false);
            }
        }, 1000),
        []
    );

    const debouncedGeocodeDestination = useCallback(
        debounce(async (address) => {
            // ... (Geocoding logika ostaje ista)
            if (!address) {
                setDestinationCoords({ lat: 0, lng: 0 });
                setFormData(prev => ({ ...prev, destinationLatitude: 0.0, destinationLongitude: 0.0 }));
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
                } else {
                    setDestinationCoords({ lat: 0, lng: 0 });
                    setFormData(prev => ({ ...prev, destinationLatitude: 0.0, destinationLongitude: 0.0 }));
                }
            } catch (err) {
                // Ignoriraj geocode greške
            } finally {
                setGeocodeLoading(false);
            }
        }, 1000),
        []
    );


    useEffect(() => {
        const loadShipment = async () => {
            if (isEditMode) {
                try {
                    const data = await fetchShipmentById(id);
                    setFormData({
                        trackingNumber: data.trackingNumber || '',
                        originAddress: data.originAddress || '',
                        destinationAddress: data.destinationAddress || '',
                        status: data.status || 'PENDING',
                        weightKg: data.weightKg || 0,
                        departureTime: formatDateTimeLocal(data.departureTime),
                        // ✅ Učitavanje očekivanog datuma
                        expectedDeliveryDate: formatDateTimeLocal(data.expectedDeliveryDate),
                        description: data.description || '',
                        originLatitude: data.originLatitude || 0.0,
                        originLongitude: data.originLongitude || 0.0,
                        destinationLatitude: data.destinationLatitude || 0.0,
                        destinationLongitude: data.destinationLongitude || 0.0,
                    });
                    setOriginCoords({ lat: data.originLatitude || 0, lng: data.originLongitude || 0 });
                    setDestinationCoords({ lat: data.destinationLatitude || 0, lng: data.destinationLongitude || 0 });

                } catch (err) {
                    setError(t('shipments.error_load'));
                } finally {
                    setLoading(false);
                }
            } else {
                setLoading(false);
            }
        };

        loadShipment();
    }, [id, isEditMode, t]);


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
                weightKg: parseFloat(formData.weightKg),
                originLatitude: parseFloat(formData.originLatitude),
                originLongitude: parseFloat(formData.originLongitude),
                destinationLatitude: parseFloat(formData.destinationLatitude),
                destinationLongitude: parseFloat(formData.destinationLongitude),
            };

            if (isEditMode) {
                await updateShipment(id, shipmentData);
                setSuccess(t('shipments.success_edit'));
            } else {
                await createShipment(shipmentData);
                setSuccess(t('shipments.success_create'));

                // Očisti formu nakon uspješnog kreiranja
                setFormData({
                    trackingNumber: '',
                    originAddress: '',
                    destinationAddress: '',
                    status: 'PENDING',
                    weightKg: 0,
                    departureTime: formatDateTimeLocal(null),
                    expectedDeliveryDate: formatDateTimeLocal(null), // ✅ Reset
                    description: '',
                    originLatitude: 0.0,
                    originLongitude: 0.0,
                    destinationLatitude: 0.0,
                    destinationLongitude: 0.0,
                });
                setOriginCoords({ lat: 0, lng: 0 });
                setDestinationCoords({ lat: 0, lng: 0 });
            }

            if (isEditMode) {
                navigate('/shipments', { state: { message: t('shipments.success_edit') } });
            }
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
        [45.8150, 15.9819];

    const initialZoom = (originCoords.lat !== 0 && destinationCoords.lat !== 0) ? 10 : 13;


    return (
        <Container className="my-5">
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace mb-4">
                        {isEditMode ? t('shipments.edit_title') : t('shipments.create_title')}
                    </h2>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}


                    <Form id="shipment-form" onSubmit={handleSubmit}>
                        {/* 1. BROJ ZA PRAĆENJE i STATUS (ostaje isto) */}
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
                                        disabled={isEditMode && formData.status === 'ASSIGNED'}
                                    >
                                        <option value="PENDING">{t('shipments.status_pending')}</option>
                                        <option value="ASSIGNED" disabled={!isEditMode || formData.status !== 'ASSIGNED'}>
                                            {t('shipments.status_assigned')}
                                        </option>
                                        <option value="IN_TRANSIT">{t('shipments.status_in_transit')}</option>
                                        <option value="DELIVERED">{t('shipments.status_delivered')}</option>
                                        <option value="CANCELLED">{t('shipments.status_cancelled')}</option>
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>


                        {/* 2. POLAZIŠTE i ODREDIŠTE (ostaje isto) */}
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

                        {/* 3. TEŽINA i DATUMI (KRITIČNA PROMJENA) */}
                        <Row className="mb-4">
                            <Col md={4}>
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

                            {/* ✅ POLJE: Datum Polaska */}
                            <Col md={4}>
                                <FloatingLabel controlId="departureTime" label={t('shipments.departure_time_label')}>
                                    <Form.Control
                                        type="datetime-local"
                                        name="departureTime"
                                        value={formData.departureTime}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* ✅ ISPRAVLJENO POLJE: expectedDeliveryDate (Usklađeno s backendom) */}
                            <Col md={4}>
                                <FloatingLabel controlId="expectedDeliveryDate" label={t('shipments.delivery_time_label')}>
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
                        </Row>

                        {/* 4. SKRIVENA POLJA ZA KOORDINATE (ostaju ista) */}
                        <Form.Control type="hidden" name="originLatitude" value={formData.originLatitude} />
                        <Form.Control type="hidden" name="originLongitude" value={formData.originLongitude} />
                        <Form.Control type="hidden" name="destinationLatitude" value={formData.destinationLatitude} />
                        <Form.Control type="hidden" name="destinationLongitude" value={formData.destinationLongitude} />


                        {/* 5. OPIS (ostaje isti) */}
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


                    </Form>

                    {/* =================================================================
                    ✅ GRUPA GUMBA ZA AKCIJU (Uključuje Detalji gumb)
                    ================================================================= */}

                    {isEditMode ? (
                        // EDIT MODE: Prikaz Detalji i Spremi
                        <div className="d-grid gap-2 d-md-flex justify-content-md-between mt-3">
                            {/* Gumb 1: Detalji (PRIKAZUJE SE SAMO U EDIT MODU) */}
                            <Button
                                variant="outline-info"
                                className="fw-bold font-monospace flex-grow-1 me-md-2"
                                onClick={() => navigate(`/shipments/details/${id}`)}
                            >
                                {t('shipments.details_button') || 'Detalji'}
                            </Button>

                            {/* Gumb 2: Spremi promjene */}
                            <Button
                                onClick={handleSubmit}
                                variant="outline-primary"
                                className="fw-bold font-monospace flex-grow-1"
                                disabled={saving}
                            >
                                {saving ? (
                                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                                ) : (
                                    t("general.save_changes") || 'Spremi promjene' // ✅ ISPRAVLJEN KLJUČ
                                )}
                            </Button>
                        </div>
                    ) : (
                        // CREATE MODE: Prikaz samo Kreiraj
                        <Button
                            onClick={handleSubmit}
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace mt-3"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                t('shipments.create_button') || 'Kreiraj Pošiljku' // ✅ ISPRAVLJEN KLJUČ
                            )}
                        </Button>
                    )}

                    {/* Gumb za Odustani (Uvijek prisutan) */}
                    <Button
                        variant="outline-secondary"
                        className="w-100 fw-bold font-monospace mt-2 mb-4"
                        onClick={() => navigate('/shipments')}
                    >
                        {t('general.cancel') || 'Odustani'}
                    </Button>


                    {/* =================================================================
                    ✅ MAPA (PRIKAZ)
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

export default ShipmentForm;
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Container, Card, Button, Form, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { geocodeAddress, createShipment } from '../services/ShipmentApi';

const customIcon = new L.Icon({
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});

function MapUpdater({ bounds }) {
    const map = useMap();
    useEffect(() => {
        if (bounds) {
            map.fitBounds(bounds, { padding: [50, 50] });
        }
    }, [map, bounds]);
    return null;
}

MapUpdater.propTypes = {
    bounds: PropTypes.oneOfType([PropTypes.array, PropTypes.object])
};

const AddShipment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        trackingNumber: 'SHIP-' + Math.random().toString(36).slice(2, 9).toUpperCase(),
        description: '',
        weightKg: 10,
        volumeM3: 0.5,
        shipmentValue: 100,
        originAddress: 'Ilica 2, Zagreb',
        destinationAddress: 'Glavna ulica 114, Sesvete',
        originLatitude: null,
        originLongitude: null,
        destinationLatitude: null,
        destinationLongitude: null,
        status: 'PENDING',
        departureTime: new Date().toISOString().slice(0, 16),
        expectedDeliveryDate: new Date(Date.now() + 86400000).toISOString().slice(0, 16)
    });

    const [mapBounds, setMapBounds] = useState(null);

    useEffect(() => {
        const debounceTimer = setTimeout(async () => {
            try {
                const p = await geocodeAddress(formData.originAddress);
                const d = await geocodeAddress(formData.destinationAddress);

                if (p && d) {
                    setFormData(prev => ({
                        ...prev,
                        originLatitude: p.lat,
                        originLongitude: p.lng,
                        destinationLatitude: d.lat,
                        destinationLongitude: d.lng
                    }));
                    setMapBounds(L.latLngBounds([[p.lat, p.lng], [d.lat, d.lng]]));
                }
            } catch (err) {
                console.error("Geocoding failed", err);
            }
        }, 800);
        return () => clearTimeout(debounceTimer);
    }, [formData.originAddress, formData.destinationAddress]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.originLatitude || !formData.destinationLatitude) {
            setError(t('shipments.waiting_coordinates') || "Čekam koordinate...");
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const shipmentData = {
                trackingNumber: formData.trackingNumber,
                description: formData.description,
                weightKg: formData.weightKg,
                volumeM3: formData.volumeM3,
                shipmentValue: formData.shipmentValue,
                originAddress: formData.originAddress,
                destinationAddress: formData.destinationAddress,
                originLatitude: formData.originLatitude,
                originLongitude: formData.originLongitude,
                destinationLatitude: formData.destinationLatitude,
                destinationLongitude: formData.destinationLongitude,
                status: formData.status,
                departureTime: formData.departureTime,
                expectedDeliveryDate: formData.expectedDeliveryDate
            };

            console.log('Šaljem na backend:', shipmentData);

            await createShipment(shipmentData);

            navigate('/shipments', {
                state: { message: t('messages.shipment_created_success') || 'Pošiljka uspješno kreirana!' }
            });
        } catch (err) {
            console.error('Greška:', err);
            setError(err.message || t('messages.shipment_create_error') || 'Greška pri kreiranju pošiljke');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-4 font-monospace">
            <Card className="shadow-lg border-0">
                <Card.Header className="bg-info text-white">
                    <h4>{t('forms.create_shipment_title')}</h4>
                </Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}

                    <Form onSubmit={handleSubmit}>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.tracking_number')} *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.trackingNumber}
                                        onChange={e => setFormData({...formData, trackingNumber: e.target.value})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.description_label')}</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.description}
                                        onChange={e => setFormData({...formData, description: e.target.value})}
                                        placeholder={t('shipments.description_placeholder') || "Opis pošiljke..."}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row>
                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.weight_label')} *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        value={formData.weightKg}
                                        onChange={e => setFormData({...formData, weightKg: Number(e.target.value)})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.volume_label')} *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        value={formData.volumeM3}
                                        onChange={e => setFormData({...formData, volumeM3: Number(e.target.value)})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.value_label')} *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        value={formData.shipmentValue}
                                        onChange={e => setFormData({...formData, shipmentValue: Number(e.target.value)})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.origin_label')} *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.originAddress}
                                        onChange={e => setFormData({...formData, originAddress: e.target.value})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.destination_label')} *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={formData.destinationAddress}
                                        onChange={e => setFormData({...formData, destinationAddress: e.target.value})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.departure_time_label')}</Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        value={formData.departureTime}
                                        onChange={e => setFormData({...formData, departureTime: e.target.value})}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>{t('shipments.delivery_time_label')} *</Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        value={formData.expectedDeliveryDate}
                                        onChange={e => setFormData({...formData, expectedDeliveryDate: e.target.value})}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <div style={{ height: '350px' }} className="mb-3 rounded border">
                            <MapContainer center={[45.815, 15.98]} zoom={7} style={{ height: '100%' }}>
                                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                                <MapUpdater bounds={mapBounds} />
                                {formData.originLatitude && (
                                    <Marker position={[formData.originLatitude, formData.originLongitude]} icon={customIcon} />
                                )}
                                {formData.destinationLatitude && (
                                    <Marker position={[formData.destinationLatitude, formData.destinationLongitude]} icon={customIcon} />
                                )}
                            </MapContainer>
                        </div>

                        <div className="d-grid gap-2">
                            <Button type="submit" variant="primary" disabled={loading} className="fw-bold">
                                {loading ? (
                                    <>
                                        <Spinner size="sm" className="me-2" />
                                        {t('general.saving') || 'Spremam...'}
                                    </>
                                ) : (
                                    t('general.save_shipment') || 'Spremi Pošiljku'
                                )}
                            </Button>
                            <Button variant="outline-secondary" onClick={() => navigate('/shipments')}>
                                {t('general.cancel')}
                            </Button>
                        </div>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddShipment;
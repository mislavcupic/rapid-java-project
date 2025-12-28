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

// ✅ SONARQUBE FIX: Dodana validacija za props
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
    bounds: PropTypes.oneOfType([
        PropTypes.array,
        PropTypes.object
    ])
};

const AddShipment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        trackingNumber: 'SHIP-' + Math.random().toString(36).substr(2, 7).toUpperCase(),
        originAddress: 'Ilica 2, Zagreb',
        destinationAddress: 'Glavna ulica 114, Sesvete',
        originLatitude: null,
        originLongitude: null,
        destinationLatitude: null,
        destinationLongitude: null,
        weightKg: 10,
        status: 'PENDING'
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
                        originLatitude: p.lat, originLongitude: p.lng,
                        destinationLatitude: d.lat, destinationLongitude: d.lng
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
            setError("Čekam koordinate...");
            return;
        }
        setLoading(true);
        try {
            await createShipment(formData);
            navigate('/shipments');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-4 font-monospace">
            <Card className="shadow-lg border-0">
                <Card.Header className="bg-info text-white"><h4>{t('forms.create_shipment_title')}</h4></Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Form onSubmit={handleSubmit}>
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Polazište</Form.Label>
                                    <Form.Control type="text" value={formData.originAddress} onChange={e => setFormData({...formData, originAddress: e.target.value})} />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Odredište</Form.Label>
                                    <Form.Control type="text" value={formData.destinationAddress} onChange={e => setFormData({...formData, destinationAddress: e.target.value})} />
                                </Form.Group>
                            </Col>
                        </Row>
                        <div style={{ height: '350px' }} className="mb-3 rounded border">
                            <MapContainer center={[45.815, 15.98]} zoom={7} style={{ height: '100%' }}>
                                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                                <MapUpdater bounds={mapBounds} />
                                {formData.originLatitude && <Marker position={[formData.originLatitude, formData.originLongitude]} icon={customIcon} />}
                                {formData.destinationLatitude && <Marker position={[formData.destinationLatitude, formData.destinationLongitude]} icon={customIcon} />}
                            </MapContainer>
                        </div>
                        <Button type="submit" variant="primary" disabled={loading}>
                            {loading ? <Spinner size="sm" /> : "Spremi"}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddShipment;
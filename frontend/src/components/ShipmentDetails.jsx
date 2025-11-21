// frontend/src/components/ShipmentDetails.jsx

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Container, Row, Col, Spinner, Alert, ListGroup, Badge, Button } from 'react-bootstrap';
import { fetchShipmentById } from '../services/ShipmentApi';
import { useTranslation } from 'react-i18next';
import { FaMapMarkerAlt, FaTruck, FaClock, FaCalendarAlt, FaWeightHanging } from 'react-icons/fa';

// =================================================================
// 🛑 UVEZI: LEAFLET
// =================================================================
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// 🛑 KRITIČNO: EKSPLICITAN UVOZ IKONA ZA LEAFLET
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';


// =================================================================
// POMOĆNE FUNKCIJE
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

// Funkcija za lijepo formatiranje datuma/vremena za prikaz
const formatDisplayDateTime = (isoString, t) => {
    if (!isoString) return t('general.not_set') || 'Nije postavljeno';
    try {
        const date = new Date(isoString);
        // Primjer formatiranja: 31.10.2025. u 18:30
        return date.toLocaleDateString(t('general.locale') || 'hr-HR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }) + 'h';
    }  catch (e) {
    console.error('Greška pri formatiranju datuma:', e);
    return t('general.invalid_date') || 'Neispravan datum';
}
}

// Pomoćna komponenta za određivanje boje statusa
const getStatusBadge = (status, t) => {
    const statusVariants = {
        'PENDING': 'warning',
        'ASSIGNED': 'primary',
        'IN_TRANSIT': 'info',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
    };

    const variant = statusVariants[status] || 'secondary';

    return (
        <Badge bg={variant} className="py-2 px-3 fw-bold font-monospace">
            {t(`shipments.status_${status.toLowerCase()}`) || status}
        </Badge>
    );

};


const ShipmentDetails = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();

    const [shipment, setShipment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadShipment = async () => {
            try {
                const data = await fetchShipmentById(id);
                setShipment(data);
            } catch (err) {
                console.error('Greška pri učitavanju shipment detalja:', err);
                setError(t( 'shipments.error_load_details') || 'Greška pri učitavanju detalja pošiljke');
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            loadShipment();
        }
    }, [id, t]);

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

    if (error) {
        return (
            <Container className="my-5">
                <Alert variant="danger" className="font-monospace">{error}</Alert>
                <Button variant="outline-secondary" onClick={() => navigate('/shipments')}>
                    {t('general.back_to_list') || 'Natrag na listu'}
                </Button>
            </Container>
        );
    }

    if (!shipment) {
        return (
            <Container className="my-5">
                <Alert variant="warning" className="font-monospace">{t('shipments.not_found') || 'Pošiljka nije pronađena.'}</Alert>
            </Container>
        );
    }

    const originCoords = {
        lat: shipment.originLatitude || 0,
        lng: shipment.originLongitude || 0
    };
    const destinationCoords = {
        lat: shipment.destinationLatitude || 0,
        lng: shipment.destinationLongitude || 0
    };

    const isMapValid = (originCoords.lat !== 0 || originCoords.lng !== 0) || (destinationCoords.lat !== 0 || destinationCoords.lng !== 0);
    const initialCenter = (originCoords.lat !== 0 || originCoords.lng !== 0) ?
        [originCoords.lat, originCoords.lng] :
        [45.815, 15.9819];
    const initialZoom = isMapValid ? 13 : 8;


    return (
        <Container className="my-5">
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace mb-4">
                        {t('shipments.details_title') || 'Detalji Pošiljke'} - #{shipment.trackingNumber}
                    </h2>

                    <Row className="mb-4">
                        <Col md={8}>
                            {/* 1. OSNOVNI PODACI I DATUMI */}
                            <ListGroup variant="flush" className="mb-4">
                                <ListGroup.Item>
                                    <FaTruck className="me-2 text-primary" />
                                    <span className="fw-bold">{t('shipments.tracking_label') || 'Broj za praćenje'}:</span>
                                    <span className="float-end font-monospace">{shipment.trackingNumber}</span>
                                </ListGroup.Item>

                                <ListGroup.Item>
                                    <FaWeightHanging className="me-2 text-secondary" />
                                    <span className="fw-bold">{t('shipments.weight_label') || 'Težina'}:</span>
                                    <span className="float-end font-monospace">{shipment.weightKg} kg</span>
                                </ListGroup.Item>

                                <ListGroup.Item>
                                    <FaCalendarAlt className="me-2 text-info" />
                                    <span className="fw-bold">{t('shipments.departure_time_label') || 'Vrijeme polaska'}:</span>
                                    <span className="float-end font-monospace">
                                        {formatDisplayDateTime(shipment.departureTime, t)}
                                    </span>
                                </ListGroup.Item>

                                <ListGroup.Item>
                                    <FaClock className="me-2 text-success" />
                                    <span className="fw-bold">{t('shipments.delivery_time_label') || 'Očekivani dolazak'}:</span>
                                    <span className="float-end font-monospace">
                                        {formatDisplayDateTime(shipment.expectedDeliveryDate, t)}
                                    </span>
                                </ListGroup.Item>

                                <ListGroup.Item>
                                    <span className="fw-bold">{t('shipments.status_label') || 'Status'}:</span>
                                    <span className="float-end">
                                        {getStatusBadge(shipment.status, t)}
                                    </span>
                                </ListGroup.Item>
                            </ListGroup>

                            {/* 2. ADRESE */}
                            <Card className="mb-4 bg-light shadow-sm">
                                <Card.Body>
                                    <Card.Title className="fw-bold text-dark font-monospace mb-3">
                                        <FaMapMarkerAlt className="me-2 text-danger" />
                                        {t('shipments.addresses') || 'Adrese'}
                                    </Card.Title>
                                    <ListGroup variant="flush">
                                        <ListGroup.Item className="bg-light">
                                            <span className="fw-bold text-danger">{t('shipments.origin_label') || 'Polazište'}:</span>
                                            <div className="float-end text-end font-monospace">{shipment.originAddress}</div>
                                        </ListGroup.Item>
                                        <ListGroup.Item className="bg-light">
                                            <span className="fw-bold text-success">{t('shipments.destination_label') || 'Odredište'}:</span>
                                            <div className="float-end text-end font-monospace">{shipment.destinationAddress}</div>
                                        </ListGroup.Item>
                                    </ListGroup>
                                </Card.Body>
                            </Card>

                            {/* 3. OPIS */}
                            <Card className="mb-4 shadow-sm">
                                <Card.Body>
                                    <Card.Title className="fw-bold text-dark font-monospace mb-3">
                                        {t('shipments.description_label') || 'Opis'}
                                    </Card.Title>
                                    <Card.Text className="text-muted font-monospace">
                                        {shipment.description || (t('general.not_available') || 'Nema opisa.')}
                                    </Card.Text>
                                </Card.Body>
                            </Card>

                            {/* GUMBI */}
                            <div className="d-grid gap-2">
                                <Button
                                    variant="outline-primary"
                                    className="fw-bold font-monospace"
                                    onClick={() => navigate(`/shipments/edit/${shipment.id}`)}
                                >
                                    {t('general.edit') || 'Uredi'}
                                </Button>
                                <Button
                                    variant="outline-secondary"
                                    className="fw-bold font-monospace"
                                    onClick={() => navigate('/shipments')}
                                >
                                    {t('general.back_to_list') || 'Natrag na listu'}
                                </Button>
                            </div>
                        </Col>

                        {/* 4. KARTA (na desnoj strani) */}
                        <Col md={4}>
                            <div className="p-3 border rounded shadow-sm sticky-top" style={{ top: '20px' }}>
                                <h5 className="text-dark fw-bold font-monospace mb-3">{t('shipments.map_title') || 'Lokacije'}</h5>

                                {isMapValid ? (
                                    <MapContainer
                                        center={initialCenter}
                                        zoom={initialZoom}
                                        scrollWheelZoom={false}
                                        style={{ height: '350px', width: '100%' }}
                                    >
                                        <TileLayer
                                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        />

                                        {originCoords.lat !== 0 && (
                                            <Marker position={originCoords} icon={customIcon}>
                                                <Popup>{t("shipments.origin_label")}: {shipment.originAddress}</Popup>
                                            </Marker>
                                        )}

                                        {destinationCoords.lat !== 0 && (
                                            <Marker position={destinationCoords} icon={customIcon}>
                                                <Popup>{t("shipments.destination_label")}: {shipment.destinationAddress}</Popup>
                                            </Marker>
                                        )}

                                    </MapContainer>
                                ) : (
                                    <Alert variant="info" className="font-monospace text-center">
                                        {t('shipments.no_coordinates') || 'Nema koordinata za prikaz karte.'}
                                    </Alert>
                                )}
                            </div>
                        </Col>
                    </Row>

                </Card.Body>
            </Card>
        </Container>
    );
};

export default ShipmentDetails;
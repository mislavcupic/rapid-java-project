import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Card, ListGroup, Badge, Button, Spinner, Alert, Row, Col } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { fetchAssignmentById } from '../services/AssignmentApi';

// 🛑 IDENTIČNO TVOJIM FAJLOVIMA
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Putanje do ikona moraju biti iste kao u AddShipment
const customIcon = new L.Icon({
    iconUrl: '/images/marker-icons/marker-icon.png',
    iconRetinaUrl: '/images/marker-icons/marker-icon-2x.png',
    shadowUrl: '/images/marker-icons/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// ✅ TVOJ MapUpdater (Rješava sivi ekran)
const MapUpdater = ({ shipments }) => {
    const map = useMap();
    useEffect(() => {
        if (shipments && shipments.length > 0) {
            const valid = shipments.filter(s => s.destinationLatitude && s.destinationLongitude);
            if (valid.length > 0) {
                // Centriraj na prvu pošiljku
                map.setView([valid[0].destinationLatitude, valid[0].destinationLongitude], 12);
            }
            // 🛑 KLJUČNI FIX ZA SIVI EKRAN (Isto kao u tvojim fajlovima)
            setTimeout(() => {
                map.invalidateSize();
            }, 600);
        }
    }, [shipments, map]);
    return null;
};

const AssignmentDetails = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchAssignmentById(id)
            .then(data => setAssignment(data))
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return <Container className="text-center py-5"><Spinner animation="border" variant="info" /></Container>;
    if (!assignment) return <Container className="py-5"><Alert variant="danger">{t('messages.resource_not_found')}</Alert></Container>;

    const shipments = assignment.shipments || [];

    return (
        <Container className="my-5">
            <Card className="shadow-lg border-0">
                <Card.Body className="p-4">
                    <h2 className="text-info fw-bold font-monospace mb-4">
                        {t('assignments.assignment_details')} #{assignment.id}
                    </h2>

                    <Row className="mb-4">
                        <Col md={6}>
                            <p className="text-muted mb-0 small fw-bold">{t('assignments.driver').toUpperCase()}</p>
                            <h5 className="font-monospace">{assignment.driver?.firstName} {assignment.driver?.lastName}</h5>
                        </Col>
                        <Col md={6}>
                            <p className="text-muted mb-0 small fw-bold">{t('assignments.vehicle').toUpperCase()}</p>
                            <h5 className="font-monospace">{assignment.vehicle?.licensePlate}</h5>
                        </Col>
                    </Row>

                    <hr className="my-4 border-info" />

                    <h5 className="text-dark fw-bold font-monospace mb-3">📍 {t('assignments.route_plan')}</h5>

                    {/* ✅ MAPA - s fiksnom visinom i MapUpdaterom */}
                    <div className="border rounded overflow-hidden shadow-sm" style={{ height: '450px', width: '100%', position: 'relative' }}>
                        <MapContainer
                            center={[45.815, 15.98]}
                            zoom={7}
                            style={{ height: '100%', width: '100%' }}
                        >
                            <TileLayer
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            />

                            <MapUpdater shipments={shipments} />

                            {shipments.map((ship) => (
                                ship.destinationLatitude && (
                                    <Marker
                                        key={ship.id}
                                        position={[ship.destinationLatitude, ship.destinationLongitude]}
                                        icon={customIcon}
                                    >
                                        <Popup className="font-monospace">
                                            <strong>{t('shipments.shipment')} #{ship.id}</strong><br/>
                                            {ship.destinationAddress}
                                        </Popup>
                                    </Marker>
                                )
                            ))}
                        </MapContainer>
                    </div>

                    <ListGroup className="mt-4 font-monospace">
                        {shipments.map((s, idx) => (
                            <ListGroup.Item key={s.id} className="d-flex justify-content-between">
                                <span>{idx + 1}. {s.destinationAddress}</span>
                                <Badge bg="info">{s.status}</Badge>
                            </ListGroup.Item>
                        ))}
                    </ListGroup>

                    <div className="mt-4">
                        <Button variant="outline-secondary" className="font-monospace px-4 me-2" onClick={() => navigate('/assignments')}>
                            {t('general.back')}
                        </Button>
                        <Button variant="outline-warning" className="font-monospace px-4" onClick={() => navigate(`/assignments/edit/${assignment.id}`)}>
                            {t('general.edit')}
                        </Button>
                    </div>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AssignmentDetails;
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Card, Button, Badge, ListGroup, Spinner, Row, Col } from 'react-bootstrap';
import { FaTruck, FaMapMarkerAlt, FaCheckCircle, FaPlay } from 'react-icons/fa';
import { fetchAssignmentDetails, startAssignment, startDelivery, completeDelivery } from '../services/DriverDashboardApi';
import DeliveryConfirmationModal from './DeliveryConfirmationModal';

const DriverAssignmentDetails = () => {
    const { id } = useParams();
    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [selectedShipment, setSelectedShipment] = useState(null);
    const [showModal, setShowModal] = useState(false);

    useEffect(() => { loadData(); }, [id]);

    const loadData = async () => {
        try {
            const data = await fetchAssignmentDetails(id);
            // Sortiranje po redoslijedu koji je izračunao algoritam
            if (data.shipments) {
                data.shipments.sort((a, b) => (a.deliverySequence || 0) - (b.deliverySequence || 0));
            }
            setAssignment(data);
        } catch (err) { console.error("Greška:", err); }
        finally { setLoading(false); }
    };

    const handleStartRoute = async () => {
        await startAssignment(id);
        loadData();
    };

    const handleStartShipment = async (sId) => {
        await startDelivery(sId);
        loadData();
    };

    const handleCompleteShipment = async (podData) => {
        // Backend traži: recipientName, notes, driverId
        await completeDelivery(selectedShipment.id, podData);
        setShowModal(false);
        loadData();
    };

    if (loading) return <Spinner className="d-block mx-auto mt-5" />;

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <h2>Ruta #{id} <Badge bg="info">{assignment?.status}</Badge></h2>
                </Col>
                <Col className="text-end">
                    {assignment?.status === 'SCHEDULED' && (
                        <Button variant="success" onClick={handleStartRoute}><FaPlay className="me-1"/> ZAPOČNI VOŽNJU</Button>
                    )}
                </Col>
            </Row>

            <ListGroup className="shadow-sm">
                {assignment?.shipments.map((s) => (
                    <ListGroup.Item key={s.id} className="p-3">
                        <Row className="align-items-center">
                            <Col xs={8}>
                                <h5 className="mb-1 text-primary">{s.deliverySequence}. {s.trackingNumber}</h5>
                                <p className="mb-0 text-muted small"><FaMapMarkerAlt/> {s.destinationAddress}</p>
                                <Badge bg={s.status === 'DELIVERED' ? 'success' : 'secondary'}>{s.status}</Badge>
                            </Col>
                            <Col xs={4} className="text-end">
                                {s.status === 'SCHEDULED' && assignment.status === 'IN_PROGRESS' && (
                                    <Button size="sm" variant="outline-primary" onClick={() => handleStartShipment(s.id)}>Kreni</Button>
                                )}
                                {s.status === 'IN_TRANSIT' && (
                                    <Button size="sm" variant="success" onClick={() => { setSelectedShipment(s); setShowModal(true); }}>
                                        <FaCheckCircle/> Isporuči
                                    </Button>
                                )}
                            </Col>
                        </Row>
                    </ListGroup.Item>
                ))}
            </ListGroup>

            <DeliveryConfirmationModal
                show={showModal}
                onHide={() => setShowModal(false)}
                onSubmit={handleCompleteShipment}
                shipment={selectedShipment}
            />
        </Container>
    );
};

export default DriverAssignmentDetails;
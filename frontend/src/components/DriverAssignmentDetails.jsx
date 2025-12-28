import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Button, Badge, ListGroup, Spinner, Row, Col } from 'react-bootstrap';
import { FaMapMarkerAlt, FaCheckCircle, FaPlay } from 'react-icons/fa';

// ✅ ISPRAVLJEN IMPORT - vučemo start i complete iz AssignmentApi
import {
    fetchAssignmentById as fetchAssignmentDetails,
    startAssignment,
    completeAssignment
} from '../services/AssignmentApi';

// Ostalo (delivery) ostaje u dashboardu ako tamo jesu
import { startDelivery, completeDelivery } from '../services/DriverDashboardApi';
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
            if (data.shipments) {
                data.shipments.sort((a, b) => (a.deliverySequence || 0) - (b.deliverySequence || 0));
            }
            setAssignment(data);
        } catch (err) {
            console.error("Greška:", err);
        } finally {
            setLoading(false);
        }
    };

    // ✅ KORISTI startAssignment
    const handleStartRoute = async () => {
        try {
            await startAssignment(id);
            alert("Vožnja započeta!");
            loadData();
        } catch (err) {
            alert("Greška: " + err.message);
        }
    };

    // ✅ KORISTI completeAssignment (pozovi ovo kad su sve pošiljke isporučene)
    const handleCompleteRoute = async () => {
        try {
            await completeAssignment(id);
            alert("Ruta uspješno završena!");
            loadData();
        } catch (err) {
            alert("Greška: " + err.message);
        }
    };

    const handleStartShipment = async (sId) => {
        try {
            await startDelivery(sId);
            loadData();
        } catch (err) {
            console.error("Greška:", err);
        }
    };

    const handleCompleteShipment = async (podData) => {
        try {
            await completeDelivery(selectedShipment.id, podData);
            setShowModal(false);
            loadData();

            // AKO JE OVO BILA ZADNJA POŠILJKA, MOŽEŠ AUTOMATSKI ZAVRŠITI RUTU
        } catch (err) {
            console.error("Greška:", err);
        }
    };

    if (loading) return <Spinner className="d-block mx-auto mt-5" />;

    return (
        <Container className="py-4 font-monospace">
            <Row className="mb-4">
                <Col>
                    <h2>Ruta #{id} <Badge bg="info">{assignment?.assignmentStatus}</Badge></h2>
                </Col>
                <Col className="text-end">
                    {/* GUMB ZA START */}
                    {assignment?.assignmentStatus === 'SCHEDULED' && (
                        <Button variant="success" onClick={handleStartRoute}>
                            <FaPlay className="me-1"/> ZAPOČNI VOŽNJU
                        </Button>
                    )}

                    {/* GUMB ZA FINISH - pojavljuje se kad je u tijeku */}
                    {assignment?.assignmentStatus === 'IN_PROGRESS' && (
                        <Button variant="outline-danger" onClick={handleCompleteRoute}>
                            ZAVRŠI CIJELU RUTU
                        </Button>
                    )}
                </Col>
            </Row>

            <ListGroup className="shadow-sm">
                {assignment?.shipments?.map((s) => (
                    <ListGroup.Item key={s.id} className="p-3">
                        <Row className="align-items-center">
                            <Col xs={8}>
                                <h5 className="mb-1 text-primary">{s.trackingNumber}</h5>
                                <p className="mb-0 text-muted small"><FaMapMarkerAlt/> {s.destinationAddress}</p>
                                <Badge bg={s.status === 'DELIVERED' ? 'success' : 'secondary'}>{s.status}</Badge>
                            </Col>
                            <Col xs={4} className="text-end">
                                {s.status === 'SCHEDULED' && assignment.assignmentStatus === 'IN_PROGRESS' && (
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
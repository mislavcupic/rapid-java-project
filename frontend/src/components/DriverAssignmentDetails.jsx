// frontend/src/components/DriverAssignmentDetails.jsx

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Card, Button, Alert, Spinner, Badge, Row, Col, Modal } from 'react-bootstrap';
import {
    fetchAssignmentDetails,
    startAssignment,
    completeAssignment,
    startDelivery,
    completeDelivery
} from '../services/DriverDashboardApi';
import {
    FaTruck,
    FaPlay,
    FaCheckCircle,
    FaBoxOpen,
    FaExclamationTriangle,
    FaMapMarkerAlt,
    FaClock
} from 'react-icons/fa';
import DeliveryConfirmationModal from './DeliveryConfirmationModal.jsx';

const DriverAssignmentDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    // Modal states
    const [showPODModal, setShowPODModal] = useState(false);
    const [showCompleteModal, setShowCompleteModal] = useState(false);

    const loadAssignment = async () => {
        try {
            const data = await fetchAssignmentDetails(id);
            setAssignment(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAssignment();
    }, [id]);

    // ========================================================================
    // ASSIGNMENT AKCIJE
    // ========================================================================

    const handleStartAssignment = async () => {
        if (!window.confirm('Želiš li započeti ovaj Assignment?')) return;

        setActionLoading(true);
        try {
            await startAssignment(id);
            await loadAssignment();
            alert('Assignment uspješno započet! ✅');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const handleCompleteAssignment = async () => {
        setShowCompleteModal(true);
    };

    const confirmCompleteAssignment = async () => {
        setShowCompleteModal(false);
        setActionLoading(true);
        try {
            await completeAssignment(id);
            await loadAssignment();
            alert('Assignment uspješno završen! 🎉');
            navigate('/driver/dashboard');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ========================================================================
    // SHIPMENT AKCIJE
    // ========================================================================

    const handleStartDelivery = async (shipmentId) => {
        if (!window.confirm('Želiš li započeti dostavu ove pošiljke?')) return;

        setActionLoading(true);
        try {
            await startDelivery(shipmentId);
            await loadAssignment();
            alert('Dostava započeta! 🚚');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    const handleCompleteDelivery = (shipmentId) => {
        setShowPODModal(shipmentId);
    };

    const handlePODSubmit = async (podData) => {
        setActionLoading(true);
        try {
            await completeDelivery(showPODModal, podData);
            await loadAssignment();
            setShowPODModal(false);
            alert('Dostava uspješno završena! ✅');
        } catch (err) {
            setError(err.message);
        } finally {
            setActionLoading(false);
        }
    };

    // ========================================================================
    // RENDERING
    // ========================================================================

    if (loading) {
        return (
            <Container className="text-center py-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3">Učitavam detalje Assignment-a...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container>
                <Alert variant="danger" className="mt-3">{error}</Alert>
                <Button variant="secondary" onClick={() => navigate('/driver/dashboard')}>
                    ← Natrag na Dashboard
                </Button>
            </Container>
        );
    }

    const shipment = assignment.shipment;
    const canStartAssignment = assignment.assignmentStatus === 'SCHEDULED';
    const canCompleteAssignment = assignment.assignmentStatus === 'IN_PROGRESS' &&
        shipment?.status === 'DELIVERED';
    const canStartDelivery = shipment?.status === 'SCHEDULED';
    const canCompleteDelivery = shipment?.status === 'IN_TRANSIT';

    return (
        <Container className="py-4">
            {/* Header */}
            <Card className="shadow-lg border-primary border-top-0 border-5 mb-4">
                <Card.Header className="bg-primary text-white">
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <h2 className="mb-0 font-monospace">Assignment #{assignment.id}</h2>
                            <small>Detaljan pregled i akcije</small>
                        </div>
                        <Badge bg={assignment.assignmentStatus === 'COMPLETED' ? 'success' : 'warning'} style={{fontSize: '1.2rem'}}>
                            {assignment.assignmentStatus}
                        </Badge>
                    </div>
                </Card.Header>
                <Card.Body>
                    <Row>
                        <Col md={6}>
                            <h5><FaTruck className="me-2 text-primary"/>Vozilo</h5>
                            <p className="fw-bold">
                                {assignment.vehicle?.licensePlate} - {assignment.vehicle?.make} {assignment.vehicle?.model}
                            </p>
                        </Col>
                        <Col md={6}>
                            <h5><FaClock className="me-2 text-primary"/>Vrijeme</h5>
                            <p>
                                <strong>Početak:</strong> {new Date(assignment.startTime).toLocaleString('hr-HR')}<br/>
                                {assignment.endTime && (
                                    <><strong>Završetak:</strong> {new Date(assignment.endTime).toLocaleString('hr-HR')}</>
                                )}
                            </p>
                        </Col>
                    </Row>

                    {/* Assignment Akcije */}
                    {canStartAssignment && (
                        <Button
                            variant="success"
                            size="lg"
                            className="w-100 mb-3 fw-bold"
                            onClick={handleStartAssignment}
                            disabled={actionLoading}
                        >
                            <FaPlay className="me-2"/> Započni Assignment
                        </Button>
                    )}

                    {canCompleteAssignment && (
                        <Button
                            variant="primary"
                            size="lg"
                            className="w-100 mb-3 fw-bold"
                            onClick={handleCompleteAssignment}
                            disabled={actionLoading}
                        >
                            <FaCheckCircle className="me-2"/> Završi Assignment
                        </Button>
                    )}
                </Card.Body>
            </Card>

            {/* Shipment Detalji */}
            <Card className="shadow-sm border-info border-2 mb-4">
                <Card.Header className="bg-info text-white">
                    <h4 className="mb-0"><FaBoxOpen className="me-2"/> Pošiljka</h4>
                </Card.Header>
                <Card.Body>
                    <Row>
                        <Col md={6}>
                            <p><strong>Tracking Number:</strong> {shipment?.trackingNumber}</p>
                            <p><strong>Status:</strong> <Badge bg="info">{shipment?.status}</Badge></p>
                        </Col>
                        <Col md={6}>
                            <p><strong>Težina:</strong> {shipment?.weight} kg</p>
                            <p><strong>Dimenzije:</strong> {shipment?.dimensions || 'N/A'}</p>
                        </Col>
                    </Row>

                    <hr/>

                    <Row>
                        <Col md={6}>
                            <h6><FaMapMarkerAlt className="text-success"/> Polazište</h6>
                            <p>{shipment?.originAddress}</p>
                        </Col>
                        <Col md={6}>
                            <h6><FaMapMarkerAlt className="text-danger"/> Odredište</h6>
                            <p>{shipment?.destinationAddress}</p>
                        </Col>
                    </Row>

                    {/* Shipment Akcije */}
                    <div className="mt-3">
                        {canStartDelivery && (
                            <Button
                                variant="outline-success"
                                className="me-2 fw-bold"
                                onClick={() => handleStartDelivery(shipment.id)}
                                disabled={actionLoading}
                            >
                                <FaPlay className="me-1"/> Započni Dostavu
                            </Button>
                        )}

                        {canCompleteDelivery && (
                            <Button
                                variant="success"
                                className="fw-bold"
                                onClick={() => handleCompleteDelivery(shipment.id)}
                                disabled={actionLoading}
                            >
                                <FaCheckCircle className="me-1"/> Potvrdi Dostavu (POD)
                            </Button>
                        )}

                        {shipment?.status === 'IN_TRANSIT' && (
                            <Button
                                variant="outline-warning"
                                className="ms-2 fw-bold"
                                onClick={() => alert('Funkcionalnost "Prijavi Problem" dolazi uskoro!')}
                            >
                                <FaExclamationTriangle className="me-1"/> Prijavi Problem
                            </Button>
                        )}
                    </div>
                </Card.Body>
            </Card>

            <Button variant="outline-secondary" onClick={() => navigate('/driver/dashboard')}>
                ← Natrag na Dashboard
            </Button>

            {/* Modali */}
            <DeliveryConfirmationModal
                show={!!showPODModal}
                onHide={() => setShowPODModal(false)}
                onSubmit={handlePODSubmit}
            />

            <Modal show={showCompleteModal} onHide={() => setShowCompleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Završi Assignment</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Jesi li siguran da želiš završiti ovaj Assignment?
                    Sve pošiljke moraju biti dostavljene!
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowCompleteModal(false)}>
                        Odustani
                    </Button>
                    <Button variant="primary" onClick={confirmCompleteAssignment}>
                        Da, završi!
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default DriverAssignmentDetails;
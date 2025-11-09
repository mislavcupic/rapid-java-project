// frontend/src/components/DriverDashboard.jsx

import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Alert, Spinner, Badge, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { fetchMySchedule } from '../services/DriverDashboardApi';
import { FaTruck, FaBoxOpen, FaClock, FaCheckCircle } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const DriverDashboard = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadSchedule = async () => {
        try {
            const data = await fetchMySchedule();
            setAssignments(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSchedule();
    }, []);

    const getStatusBadge = (status) => {
        switch (status) {
            case 'SCHEDULED':
                return <Badge bg="warning" text="dark"><FaClock className="me-1"/> Zakazano</Badge>;
            case 'IN_PROGRESS':
                return <Badge bg="info"><FaTruck className="me-1"/> U tijeku</Badge>;
            case 'COMPLETED':
                return <Badge bg="success"><FaCheckCircle className="me-1"/> Završeno</Badge>;
            default:
                return <Badge bg="secondary">{status}</Badge>;
        }
    };

    const getShipmentStatusBadge = (status) => {
        switch (status) {
            case 'PENDING':
                return <Badge bg="secondary">Čeka</Badge>;
            case 'SCHEDULED':
                return <Badge bg="warning" text="dark">Zakazano</Badge>;
            case 'IN_TRANSIT':
                return <Badge bg="info">U dostavi</Badge>;
            case 'DELIVERED':
                return <Badge bg="success">Dostavljeno</Badge>;
            case 'DELAYED':
                return <Badge bg="danger">Kašnjenje</Badge>;
            case 'CANCELLED':
                return <Badge bg="dark">Otkazano</Badge>;
            default:
                return <Badge bg="secondary">{status}</Badge>;
        }
    };

    if (loading) {
        return (
            <Container className="text-center py-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3">Učitavam tvoj raspored...</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container>
                <Alert variant="danger" className="mt-3">
                    <strong>Greška:</strong> {error}
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <Card className="shadow-lg border-primary border-top-0 border-5 mb-4">
                <Card.Header className="bg-primary text-white">
                    <h2 className="mb-0 font-monospace">
                        <FaTruck className="me-2"/> Moj Raspored
                    </h2>
                    <small>Pregled tvojih Assignment-a za danas</small>
                </Card.Header>
                <Card.Body>
                    {assignments.length === 0 ? (
                        <Alert variant="info" className="text-center font-monospace">
                            <FaBoxOpen size={48} className="mb-3 d-block mx-auto"/>
                            Nemaš aktivnih Assignment-a danas. Uživaj u slobodnom danu! 🎉
                        </Alert>
                    ) : (
                        <Row>
                            {assignments.map((assignment) => (
                                <Col md={6} lg={4} key={assignment.id} className="mb-4">
                                    <Card
                                        className="h-100 shadow-sm border-2 hover-lift"
                                        style={{ cursor: 'pointer', transition: 'transform 0.2s' }}
                                        onClick={() => navigate(`/driver/assignment/${assignment.id}`)}
                                    >
                                        <Card.Header className="bg-light">
                                            <div className="d-flex justify-content-between align-items-center">
                                                <strong className="font-monospace">Assignment #{assignment.id}</strong>
                                                {getStatusBadge(assignment.assignmentStatus)}
                                            </div>
                                        </Card.Header>
                                        <Card.Body>
                                            <div className="mb-2">
                                                <small className="text-muted">Vozilo:</small>
                                                <div className="fw-bold">
                                                    {assignment.vehicle?.licensePlate} ({assignment.vehicle?.make})
                                                </div>
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted">Pošiljka:</small>
                                                <div className="d-flex justify-content-between">
                                                    <span className="fw-bold">{assignment.shipment?.trackingNumber}</span>
                                                    {getShipmentStatusBadge(assignment.shipment?.status)}
                                                </div>
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted">Odredište:</small>
                                                <div className="text-truncate">
                                                    {assignment.shipment?.destinationAddress || 'N/A'}
                                                </div>
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted"><FaClock className="me-1"/> Početak:</small>
                                                <div>{new Date(assignment.startTime).toLocaleString('hr-HR')}</div>
                                            </div>

                                            {assignment.endTime && (
                                                <div className="mb-2">
                                                    <small className="text-muted">Završetak:</small>
                                                    <div>{new Date(assignment.endTime).toLocaleString('hr-HR')}</div>
                                                </div>
                                            )}
                                        </Card.Body>
                                        <Card.Footer className="bg-transparent">
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="w-100 fw-bold font-monospace"
                                            >
                                                Otvori Detalje →
                                            </Button>
                                        </Card.Footer>
                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    )}
                </Card.Body>
            </Card>

            {/* Statistika */}
            <Row>
                <Col md={4}>
                    <Card className="text-center shadow-sm border-warning border-2">
                        <Card.Body>
                            <FaClock size={32} className="text-warning mb-2"/>
                            <h3>{assignments.filter(a => a.assignmentStatus === 'SCHEDULED').length}</h3>
                            <small className="text-muted">Zakazano</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center shadow-sm border-info border-2">
                        <Card.Body>
                            <FaTruck size={32} className="text-info mb-2"/>
                            <h3>{assignments.filter(a => a.assignmentStatus === 'IN_PROGRESS').length}</h3>
                            <small className="text-muted">U tijeku</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center shadow-sm border-success border-2">
                        <Card.Body>
                            <FaCheckCircle size={32} className="text-success mb-2"/>
                            <h3>{assignments.filter(a => a.assignmentStatus === 'COMPLETED').length}</h3>
                            <small className="text-muted">Završeno</small>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default DriverDashboard;
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
                return <Badge bg="warning" text="dark"><FaClock className="me-1"/> {t('status.scheduled')}</Badge>;
            case 'IN_PROGRESS':
                return <Badge bg="info"><FaTruck className="me-1"/> {t('status.in_progress')}</Badge>;
            case 'COMPLETED':
                return <Badge bg="success"><FaCheckCircle className="me-1"/> {t('status.completed')}</Badge>;
            default:
                return <Badge bg="secondary">{status}</Badge>;
        }
    };

    if (loading) {
        return (
            <Container className="text-center py-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3">{t('general.loading_data')}</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container>
                <Alert variant="danger" className="mt-3">
                    <strong>{t('error.general_error')}:</strong> {error}
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="py-4 font-monospace">
            <Card className="shadow-lg border-primary border-top-0 border-5 mb-4">
                <Card.Header className="bg-primary text-white">
                    <h2 className="mb-0">
                        <FaTruck className="me-2"/> {t('driver.my_schedule')}
                    </h2>
                    <small>{t('driver.today_overview')}</small>
                </Card.Header>
                <Card.Body>
                    {assignments.length === 0 ? (
                        <Alert variant="info" className="text-center">
                            <FaBoxOpen size={48} className="mb-3 d-block mx-auto"/>
                            {t('driver.no_assignments')}
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
                                                <strong>{t('assignments.route')} #{assignment.id}</strong>
                                                {getStatusBadge(assignment.assignmentStatus)}
                                            </div>
                                        </Card.Header>
                                        <Card.Body>
                                            <div className="mb-2">
                                                <small className="text-muted">{t('shipments.vehicle')}:</small>
                                                <div className="fw-bold">
                                                    {assignment.vehicle?.licensePlate} ({assignment.vehicle?.make})
                                                </div>
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted">{t('shipments.shipments')}:</small>
                                                {assignment.shipments && assignment.shipments.length > 0 ? (
                                                    <>
                                                        <div className="fw-bold">
                                                            {assignment.shipments.length} {t('shipments.count')}
                                                        </div>
                                                        <div className="small text-muted text-truncate">
                                                            {assignment.shipments.map(s => s.trackingNumber).join(', ')}
                                                        </div>
                                                    </>
                                                ) : (
                                                    <div className="text-muted">{t('shipments.no_shipments')}</div>
                                                )}
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted">{t('shipments.destinations')}:</small>
                                                <div className="text-truncate">
                                                    {assignment.shipments?.[0]?.destinationAddress || 'N/A'}
                                                    {assignment.shipments?.length > 1 && (
                                                        <span className="text-muted"> +{assignment.shipments.length - 1} {t('general.more')}</span>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="mb-2">
                                                <small className="text-muted"><FaClock className="me-1"/> {t('shipments.departure_time')}:</small>
                                                <div>{new Date(assignment.startTime).toLocaleString('hr-HR')}</div>
                                            </div>

                                            {assignment.endTime && (
                                                <div className="mb-2">
                                                    <small className="text-muted">{t('assignments.end_time')}:</small>
                                                    <div>{new Date(assignment.endTime).toLocaleString('hr-HR')}</div>
                                                </div>
                                            )}
                                        </Card.Body>
                                        <Card.Footer className="bg-transparent">
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="w-100 fw-bold"
                                            >
                                                {t('general.open_details')} →
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
                            <small className="text-muted">{t('status.scheduled')}</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center shadow-sm border-info border-2">
                        <Card.Body>
                            <FaTruck size={32} className="text-info mb-2"/>
                            <h3>{assignments.filter(a => a.assignmentStatus === 'IN_PROGRESS').length}</h3>
                            <small className="text-muted">{t('status.in_progress')}</small>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={4}>
                    <Card className="text-center shadow-sm border-success border-2">
                        <Card.Body>
                            <FaCheckCircle size={32} className="text-success mb-2"/>
                            <h3>{assignments.filter(a => a.assignmentStatus === 'COMPLETED').length}</h3>
                            <small className="text-muted">{t('status.completed')}</small>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default DriverDashboard;
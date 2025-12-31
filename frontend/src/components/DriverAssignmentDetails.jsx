import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Container, Button, Badge, ListGroup, Spinner, Row, Col, Modal, Form, Alert } from 'react-bootstrap';
import { FaMapMarkerAlt, FaCheckCircle, FaPlay, FaExclamationTriangle } from 'react-icons/fa';

import {
    fetchAssignmentById as fetchAssignmentDetails,
    startAssignment,
    completeAssignment
} from '../services/AssignmentApi';

import { startDelivery, completeDelivery, reportIssue } from '../services/DriverDashboardApi';
import DeliveryConfirmationModal from './DeliveryConfirmationModal';

const DriverAssignmentDetails = () => {
    const { id } = useParams();
    const { t } = useTranslation();

    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedShipment, setSelectedShipment] = useState(null);
    const [showModal, setShowModal] = useState(false);

    const [showIssueModal, setShowIssueModal] = useState(false);
    const [issueData, setIssueData] = useState({
        issueType: '',
        description: '',
        estimatedDelay: ''
    });

    useEffect(() => { loadData(); }, [id]);

    const loadData = async () => {
        try {
            setError(null);
            const data = await fetchAssignmentDetails(id);
            if (data.shipments) {
                data.shipments.sort((a, b) => (a.deliverySequence || 0) - (b.deliverySequence || 0));
            }
            setAssignment(data);
        } catch (err) {
            setError(t('common.error_loading') || "Greška pri učitavanju rute, {}",err);
        } finally {
            setLoading(false);
        }
    };

    const getPosition = () => {
        return new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject);
        });
    };

    const handleStartRoute = async () => {
        try {
            setError(null);
            await startAssignment(id);
            await loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleCompleteRoute = async () => {
        try {
            setError(null);
            await completeAssignment(id);
            alert(t('assignments.completed_success') || "Ruta uspješno završena!");
            await loadData();
        } catch (err) {
            // Ovdje se hvata ConflictException ako svi paketi nisu isporučeni
            setError(err.message);
        }
    };

    const handleStartShipment = async (sId) => {
        try {
            setError(null);
            // ✅ ISPRAVLJENO: Dodana oba argumenta (shipmentId i driverId)
            // driverId uzimamo iz učitanog assignment objekta
            await startDelivery(sId, assignment.driverId);
            await loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleCompleteShipment = async (podData) => {
        try {
            setError(null);
            const pos = await getPosition();
            const finalPodData = {
                ...podData,
                latitude: pos.coords.latitude,
                longitude: pos.coords.longitude
            };
            await completeDelivery(selectedShipment.id, finalPodData);
            setShowModal(false);
            await loadData();
        } catch (err) {
            setError(err.code ? t('issue.gps_error') : err.message);
        }
    };

    const handleReportIssue = async (e) => {
        e.preventDefault();
        try {
            setError(null);
            const pos = await getPosition();

            // ✅ USKLAĐENO S IssueReportDTO.java (bez driverId)
            const payload = {
                issueType: issueData.issueType,
                description: issueData.description,
                estimatedDelay: issueData.estimatedDelay,
                latitude: pos.coords.latitude,
                longitude: pos.coords.longitude
            };

            await reportIssue(selectedShipment.id, payload);
            setShowIssueModal(false);
            setIssueData({ issueType: '', description: '', estimatedDelay: '' });
            await loadData();
        } catch (err) {
            setError(err.message || t('common.unexpected_error'));
            setShowIssueModal(false);
        }
    };

    if (loading) return <Spinner className="d-block mx-auto mt-5" />;

    return (
        <Container className="py-4 font-monospace">
            {error && (
                <Alert variant="danger" onClose={() => setError(null)} dismissible className="shadow-sm">
                    <FaExclamationTriangle className="me-2"/> {error}
                </Alert>
            )}

            <Row className="mb-4">
                <Col>
                    <h2>{t('assignments.route') || "Ruta"} #{id} <Badge bg="info">{assignment?.assignmentStatus}</Badge></h2>
                </Col>
                <Col className="text-end">
                    {assignment?.assignmentStatus === 'SCHEDULED' && (
                        <Button variant="success" onClick={handleStartRoute}>
                            <FaPlay className="me-1"/> {t('assignments.start_time') || "ZAPOČNI VOŽNJU"}
                        </Button>
                    )}
                    {assignment?.assignmentStatus === 'IN_PROGRESS' && (
                        <Button variant="outline-danger" onClick={handleCompleteRoute}>
                            {t('assignments.end_time') || "ZAVRŠI CIJELU RUTU"}
                        </Button>
                    )}
                </Col>
            </Row>

            <ListGroup className="shadow-sm">
                {assignment?.shipments?.map((s) => (
                    <ListGroup.Item key={s.id} className="p-3">
                        <Row className="align-items-center">
                            <Col xs={7}>
                                <h5 className="mb-1 text-primary">{s.trackingNumber}</h5>
                                <p className="mb-0 text-muted small"><FaMapMarkerAlt/> {s.destinationAddress}</p>
                                <Badge bg={s.status === 'DELIVERED' ? 'success' : 'secondary'}>{s.status}</Badge>
                            </Col>
                            <Col xs={5} className="text-end d-flex gap-2 justify-content-end">
                                {s.status === 'SCHEDULED' && assignment.assignmentStatus === 'IN_PROGRESS' && (
                                    <Button size="sm" variant="outline-primary" onClick={() => handleStartShipment(s.id)}>
                                        {t('common.start') || "Kreni"}
                                    </Button>
                                )}

                                {/* ✅ Omogućeno "Isporuči" i za IN_TRANSIT i za pošiljke s problemom */}
                                {(s.status === 'IN_TRANSIT' || s.status === 'ISSUE_REPORTED' || s.status === 'DELAYED') && (
                                    <>
                                        <Button size="sm" variant="success" onClick={() => { setSelectedShipment(s); setShowModal(true); }}>
                                            <FaCheckCircle/> {t('common.deliver') || "Isporuči"}
                                        </Button>

                                        {/* Pokaži gumb za problem samo ako već nije prijavljen */}
                                        {s.status === 'IN_TRANSIT' && (
                                            <Button size="sm" variant="danger" onClick={() => { setSelectedShipment(s); setShowIssueModal(true); }}>
                                                <FaExclamationTriangle/> {t('common.issue') || "Problem"}
                                            </Button>
                                        )}
                                    </>
                                )}
                            </Col>
                        </Row>
                    </ListGroup.Item>
                ))}
            </ListGroup>

            {/* MODAL ZA PRIJAVU PROBLEMA */}
            <Modal show={showIssueModal} onHide={() => setShowIssueModal(false)} centered>
                <Modal.Header closeButton className="bg-danger text-white">
                    <Modal.Title>{t('issue.report_title') || "Prijavi problem"}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleReportIssue}>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('issue.type') || "Tip problema"}</Form.Label>
                            <Form.Select
                                required
                                value={issueData.issueType}
                                onChange={e => setIssueData({...issueData, issueType: e.target.value})}
                            >
                                <option value="">{t('common.select') || "Odaberi..."}</option>
                                <option value="ADDRESS_INCORRECT">ADDRESS_INCORRECT</option>
                                <option value="RECIPIENT_UNAVAILABLE">RECIPIENT_UNAVAILABLE</option>
                                <option value="VEHICLE_ISSUE">VEHICLE_ISSUE</option>
                                <option value="OTHER">OTHER</option>
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>{t('issue.description') || "Opis"}</Form.Label>
                            <Form.Control
                                as="textarea" rows={3} required
                                value={issueData.description}
                                onChange={e => setIssueData({...issueData, description: e.target.value})}
                            />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>{t('issue.estimated_delay') || "Kašnjenje"}</Form.Label>
                            <Form.Control
                                type="text"
                                value={issueData.estimatedDelay}
                                onChange={e => setIssueData({...issueData, estimatedDelay: e.target.value})}
                                placeholder="npr. 4 days"
                            />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowIssueModal(false)}>{t('common.cancel') || "Odustani"}</Button>
                        <Button variant="danger" type="submit">{t('common.submit') || "Pošalji"}</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

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
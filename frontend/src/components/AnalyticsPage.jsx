// src/components/Analytics/AnalyticsPage.jsx (AŽURIRAN)

import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner, Modal, Table } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import styles from '../Analytics.module.css';

// Uvoz service funkcija za ANALITIKU (za brojeve)
import {
    getAverageActiveShipmentWeight,
    bulkMarkOverdue,
} from '../services/AnalyticsService.js';

// 🆕 Uvoz service funkcija za DETALJNE LISTE (iz VehicleApi.js)
import {
    fetchOverdueVehicles,
    fetchWarningVehicles,
    fetchFreeVehiclesDetails
} from '../services/VehicleApi.js';

// --- POMOĆNE FUNKCIJE OSTALE ISTE ---

const getAuthData = () => {
    try {
        const token = localStorage.getItem('accessToken');
        const roleString = localStorage.getItem('userRole');
        const rolesArray = roleString ? [roleString] : [];
        return {
            token: token,
            roles: rolesArray,
            user: null
        };
    } catch (e) {
        return { token: null, roles: [], user: null };
    }
};

const formatNumber = (num, unit = '') => {
    const value = num !== null && num !== undefined ? num : 0;
    return value.toLocaleString('hr-HR', { maximumFractionDigits: 2 }) + unit;
};

// =================================================================
// 🚀 GLAVNA KOMPONENTA
// =================================================================

const AnalyticsPage = () => {
    const { t } = useTranslation();

    // --- STANJE ZA AGREGIRANE VRIJEDNOSTI ---
    const [auth, setAuth] = useState(getAuthData());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [avgWeight, setAvgWeight] = useState(0);
    const [analytics, setAnalytics] = useState({ overdue: 0, warning: 0, free: 0, total: 0 });

    // --- STANJE ZA BULK OPERACIJE ---
    const [isLoading, setIsLoading] = useState(false);
    const [overdueMessage, setOverdueMessage] = useState('');

    // --- 🆕 STANJE ZA DETALJNE LISTE ---
    const [overdueList, setOverdueList] = useState([]);
    const [warningList, setWarningList] = useState([]);
    const [freeList, setFreeList] = useState([]);

    // --- STANJE ZA MODALE ---
    const [showModal, setShowModal] = useState(null);

    // --- UTILITY MEMO VRIJEDNOSTI ---
    const canViewAnalytics = auth.roles.includes('ROLE_ADMIN') || auth.roles.includes('ROLE_DISPATCHER');
    const isAdmin = auth.roles.includes('ROLE_ADMIN');
    const token = auth.token;

    // =================================================================
    // 📄 UČITAVANJE PODATAKA
    // =================================================================

    const loadData = useCallback(async () => {
        if (!canViewAnalytics || !token) {
            setLoading(false);
            return;
        }

        try {
            setLoading(true);

            // Dohvat prosječne težine
            const weight = await getAverageActiveShipmentWeight(token);
            setAvgWeight(weight);

            // 🆕 Dohvat detaljnih listi
            const [ovList, waList, frList] = await Promise.all([
                fetchOverdueVehicles(),
                fetchWarningVehicles(),
                fetchFreeVehiclesDetails()
            ]);

            setOverdueList(ovList);
            setWarningList(waList);
            setFreeList(frList);

            // ⭐ Postavi brojeve za kartice na temelju stvarnih podataka
            setAnalytics({
                overdue: ovList.length,
                warning: waList.length,
                free: frList.length,
                total: ovList.length + waList.length + frList.length
            });

            setError(null);
        } catch (err) {
            setError(err.message || 'Greška pri dohvaćanju analitičkih podataka.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [token, canViewAnalytics]);

    useEffect(() => {
        loadData();
    }, [loadData]);

    // =================================================================
    // 🚀 BULK ACTION: Označi overdue
    // =================================================================
    const handleBulkMarkOverdue = async () => {
        if (!isAdmin) {
            alert('Samo ADMINISTRATOR smije izvršiti ovu akciju.');
            return;
        }

        const confirmAction = globalThis.confirm( /*ovdje mi je sonarqube rekao da umjesto window koristim*/
            'Jeste li sigurni da želite OZNAČITI SVA VOZILA s PREKORAČENIM SERVISOM kao OVERDUE? Ova akcija je nepovratna!'
        );
        if (!confirmAction) return;

        try {
            setIsLoading(true);
            const result = await bulkMarkOverdue(token);
            setOverdueMessage(result.message || 'Bulk UPDATE izvršen.');
        } catch (err) {
            console.error('Greška pri bulk update-u:', err);
            setOverdueMessage(err.message || 'Greška pri bulk update-u.');
        } finally {
            setIsLoading(false);
        }
    };


    const openModalForList = useCallback(
        async (type) => {
            if (!token) return;

            try {
                setShowModal(type);
                if (type === 'overdue') {
                    const data = await fetchOverdueVehicles(token);
                    setOverdueList(data || []);
                } else if (type === 'warning') {
                    const data = await fetchWarningVehicles(token);
                    setWarningList(data || []);
                } else if (type === 'free') {
                    const data = await fetchFreeVehiclesDetails(token);
                    setFreeList(data || []);
                }
            } catch (err) {
                console.error(`Greška pri dohvaćanju ${type} liste:`, err);
            }
        },
        [token]
    );

    const closeModal = () => {
        setShowModal(null);
    };


    const renderModal = () => {
        if (!showModal) return null;

        let title = '';
        let listToShow = [];
        let columns = [];

        if (showModal === 'overdue') {
            title = 'PREKORAČEN SERVIS (Overdue)';
            listToShow = overdueList;
            columns = ['driver', 'km'];
        } else if (showModal === 'warning') {
            title = 'UPOZORENJE (Warning): Blizu servisnog roka';
            listToShow = warningList;
            columns = ['driver', 'km'];
        } else if (showModal === 'free') {
            title = 'SLOBODNA VOZILA (Free)';
            listToShow = freeList;
            columns = ['driver'];
        }

        return (
            <Modal show={true} onHide={closeModal} size="lg" centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace">{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Table striped bordered hover size="sm" className='font-monospace'>
                        <thead>
                        <tr>
                            <th>{t("general.index")}</th>
                            <th>{t("general.vehicle")}</th>
                            <th>{t("general.reg_mark")}</th>
                            {columns.includes('driver') && <th>{t("general.current_driver")}</th>}
                            {columns.includes('km') && <th>{t("general.remaining_km")}</th>}
                        </tr>
                        </thead>
                        <tbody>
                        {listToShow.map((item, index) => (
                            <tr key={item.id}>
                                <td>{index + 1}</td>
                                <td>{item.name}</td>
                                <td>{item.licensePlate}</td>
                                <td className={item.driver === 'N/A' ? 'text-danger' : 'text-success'}>{item.driver}</td>
                                {columns.includes('km') && <td>{item.remainingKm} km</td>}
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                    {listToShow.length === 0 && (
                        <p className="text-center text-muted">{t("messages.no_data")}</p>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={closeModal} className='font-monospace'>
                        {t("general.close")}
                    </Button>
                </Modal.Footer>
            </Modal>
        );
    };



    if (!canViewAnalytics) {
        return (
            <Container className="mt-5">
                <Alert variant="danger" className='font-monospace'>
                    <strong>Pristup odbijen!</strong> {t("messages.access_denied")}
                </Alert>
            </Container>
        );
    }

    if (loading) {
        return (
            <Container className="mt-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-3 text-muted font-monospace">{t("general.loading_data")}</p>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="mt-5">
                <Alert variant="warning" className='font-monospace'>
                    <strong>Greška:</strong> {error}
                </Alert>
            </Container>
        );
    }

    // --- ✅ ISPRAVLJENA DEFINICIJA KARTICA SA t() ---
    const cards = [
        {
            title: t('alerts.overdue_service'),
            count: analytics.overdue,
            variant: 'danger',
            description: t('analytics.unassigned_vehicles'),
            onClick: () => openModalForList('overdue'),
        },
        {
            title: t('alerts.service_warning'),
            count: analytics.warning,
            variant: 'warning',
            description: 'Vozila blizu servisnog roka.',
            onClick: () => openModalForList('warning'),
        },
        {
            title: t('alerts.free_vehicles'),
            count: analytics.free,
            variant: 'success',
            description: 'Vozila bez dodijeljenog vozača.',
            onClick: () => openModalForList('free'),
        },
        {
            title: t('alerts.total_vehicles'),
            count: analytics.total,
            variant: 'info',
            description: 'Ukupan broj vozila u floti.',
            onClick: null,
        },
        {
            title: t('alerts.average_weight'),
            count: formatNumber(avgWeight, ' kg'),
            variant: 'primary',
            description: 'Prosječna težina trenutno AKTIVNIH pošiljaka.',
            onClick: null,
        },
    ];

    return (
        <Container className="mt-4">
            <Row className="mb-4">
                <Col>
                    <h1 className="h3 fw-bold font-monospace">{t("general.dashboard_title")}</h1>
                    <p className="text-muted font-monospace">
                        {t("general.status_ready")}
                    </p>
                </Col>
            </Row>

            {/* Klikabilne Kartice */}
            <Row className="mb-4 g-4">
                {cards.map((card, idx) => (
                    <Col key={idx} xs={12} md={6} lg={4}>
                        <Card
                            className={`shadow-sm border-${card.variant} h-100 ${
                                card.onClick ? 'cursor-pointer hover-shadow' : ''
                            }`}
                            onClick={card.onClick}
                            style={{ cursor: card.onClick ? 'pointer' : 'default' }}
                        >
                            <Card.Body>
                                <Card.Title className={`text-${card.variant} fw-bold font-monospace`}>
                                    {card.title}
                                </Card.Title>
                                <h2 className={`display-4 text-${card.variant} font-monospace`}>
                                    {card.count}
                                </h2>
                                <Card.Text className="text-muted small font-monospace">
                                    {card.description}
                                </Card.Text>
                            </Card.Body>
                        </Card>
                    </Col>
                ))}
            </Row>

            {/* Bulk Action Section (samo ADMIN) */}
            {isAdmin && (
                <Row className="mb-4">
                    <Col>
                        <Card className="shadow-sm">
                            <Card.Body>
                                <h5 className="text-danger fw-bold font-monospace">
                                    {t("analytics.bulk_action_title")}
                                </h5>
                                <p className="text-muted small font-monospace">
                                    {t("analytics.bulk_action_text")}
                                </p>
                                <Button
                                    variant="danger"
                                    onClick={handleBulkMarkOverdue}
                                    disabled={isLoading}
                                    className='font-monospace'
                                >
                                    {isLoading ? t("general.executing_dml") : t("analytics.execute_bulk_update")}
                                </Button>
                                {overdueMessage && (
                                    <Alert variant="info" className="mt-3 font-monospace">
                                        {overdueMessage}
                                    </Alert>
                                )}
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            )}

            {/* Render Modal */}
            {renderModal()}
        </Container>
    );
};

export default AnalyticsPage;
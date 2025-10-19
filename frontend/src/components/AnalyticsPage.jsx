// src/components/Analytics/AnalyticsPage.jsx

import React, { useState, useEffect, useMemo } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import styles from '../Analytics.module.css';

// Uvoz service funkcija
import { getAverageActiveShipmentWeight, bulkMarkOverdue } from '../services/AnalyticsService.js';

// --- FUNKCIJA ZA ČITANJE KORISNIKA IZ LOCAL STORAGE ---
const getAuthData = () => {
    try {
        // KORIGIRANO: Ključ za JWT token je 'accessToken' (prema Local Storage-u)
        const token = localStorage.getItem('accessToken');

        // KORIGIRANO: Čitamo userRole (string), a ne currentUser
        const roleString = localStorage.getItem('userRole');

        // Stvaramo niz uloga ([roleString] umjesto userData?.roles)
        const rolesArray = roleString ? [roleString] : [];

        // Vraćamo objekt s tokenom i korigiranim ulogama
        return {
            token: token,
            roles: rolesArray,
            user: null // Nije potrebno ako se ne koristi cijeli objekt
        };
    } catch (e) {
        console.error("Error reading auth data from localStorage:", e);
        return { token: null, roles: [], user: null };
    }
};

const AnalyticsPage = () => {

    // Čitanje podataka pri renderu
    // useMemo je zadržan, ali poziva korigiranu getAuthData funkciju
    const { token, roles, user } = useMemo(() => getAuthData(), []);

    const [avgWeight, setAvgWeight] = useState('...');
    const [overdueMessage, setOverdueMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [fetchError, setFetchError] = useState(null);

    // Provjera dozvola na temelju uloga pročitanih iz localStorage
    const isAdmin = roles.includes('ROLE_ADMIN');
    const isDispatcher = roles.includes('ROLE_DISPATCHER');
    const canViewAnalytics = token && (isAdmin || isDispatcher);

    // 1. Dohvat analitičkih podataka
    useEffect(() => {
        // Ako canViewAnalytics nije TRUE, prekini (ovo sada radi jer token više nije NULL)
        if (!canViewAnalytics) {
            if (token) {
                // Ako token postoji, ali nema ulogu
                setFetchError('STATUS: FAILED. Nemate potrebnu ulogu za analitiku.');
            }
            return;
        }

        setFetchError(null);

        getAverageActiveShipmentWeight(token)
            .then(data => {
                setAvgWeight(`${data.toFixed(2)} kg`);
            })
            .catch(error => {
                setFetchError('STATUS: FAILED. Provjerite da li je Backend pokrenut i JWT validan.');
                setAvgWeight('N/A');
                console.error("Analytics fetch error:", error);
            });
    }, [token, canViewAnalytics]);

    // 2. Bulk operacija (SAMO ZA ADMINA)
    const handleMarkOverdue = async () => {
        if (!isAdmin) return;

        setIsLoading(true);
        setOverdueMessage('INITIATING DML COMMAND...');

        bulkMarkOverdue(token)
            .then(message => {
                setOverdueMessage(`[SUCCESS] ${message}`);
            })
            .catch(error => {
                setOverdueMessage(`[ERROR] Ažuriranje neuspjelo. Poruka: ${error.message.substring(0, 100)}`);
                console.error("Bulk update error:", error);
            })
            .finally(() => {
                setIsLoading(false);
            });
    };

    // Prikaz greške za neovlašteni pristup ili neprijavljenog korisnika
    if (!token) {
        return (
            <Container fluid className={`${styles.courierFont} p-4 bg-dark min-vh-100`}>
                <Alert variant="danger" className={styles.terminalCard}>
                    ACCESS DENIED: Morate se prijaviti za pristup.
                </Alert>
            </Container>
        );
    }

    // Prikaz ako je prijavljen ali nema ulogu za analitiku
    if (token && !canViewAnalytics) {
        return (
            <Container fluid className={`${styles.courierFont} p-4 bg-dark min-vh-100`}>
                <Alert variant="danger" className={styles.terminalCard}>
                    COMMAND DENIED: Dozvola za ulogu '{roles.join(', ')}' je ograničena.
                </Alert>
            </Container>
        );
    }

    // Glavni prikaz (dostupan samo ADMIN i DISPATCHER)
    return (
        <Container fluid className={`${styles.courierFont} p-4 bg-dark min-vh-100`}>
            <h1 className='mb-4 text-white'>// FLEET ANALYTICS CONSOLE</h1>

            {fetchError && <Alert variant="warning" className={styles.terminalCard}><span className='text-danger'>{fetchError}</span></Alert>}

            <Row>
                {/* 1. KARTICA: ANALITIKA (ADMIN/DISPATCHER) */}
                <Col md={6} className='mb-4'>
                    <Card className={styles.terminalCard}>
                        <Card.Header as="h5" className='text-info border-secondary'>
                            {'>'} SYSTEM_REPORT_01_WEIGHT_AVG
                        </Card.Header>
                        <Card.Body>
                            <Card.Title className='text-muted'>PROSJEČNA TEŽINA AKTIVNIH POŠILJAKA (KG)</Card.Title>
                            <Card.Text>
                                <span className='text-white' style={{ fontSize: '3rem', fontWeight: 'bold' }}>{avgWeight}</span>
                            </Card.Text>
                            <Button variant="outline-info" size="sm">
                                POGLED UPIT (SQL)
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>

                {/* 2. KARTICA: BULK OPERACIJE (SAMO za ADMINA) */}
                {isAdmin && (
                    <Col md={6} className='mb-4'>
                        <Card className={styles.terminalCard}>
                            <Card.Header as="h5" className='text-warning border-secondary'>
                                {'>'} ADMIN_ACTION_02_BULK_UPDATE
                            </Card.Header>
                            <Card.Body>
                                <Card.Title className='text-danger'>MASOVNA AKCIJA: MARK OVERDUE</Card.Title>
                                <Card.Text className='text-muted'>
                                    POZOR: Ova akcija pokreće izravni DML upit na bazi.
                                </Card.Text>

                                <Button
                                    variant="custom"
                                    className={styles.pastelButton}
                                    onClick={handleMarkOverdue}
                                    disabled={isLoading}
                                >
                                    {isLoading ? <><Spinner animation="border" size="sm" className='me-2'/>EXECUTING DML...</> : 'EXECUTE BULK UPDATE'}
                                </Button>

                                <p className='mt-3'>
                                    <span style={{ color: overdueMessage.includes('ERROR') ? '#ff4d4d' : '#90ee90' }}>
                                        {overdueMessage || 'Status: READY'}
                                    </span>
                                </p>
                            </Card.Body>
                        </Card>
                    </Col>
                )}
            </Row>
        </Container>
    );
};

export default AnalyticsPage;
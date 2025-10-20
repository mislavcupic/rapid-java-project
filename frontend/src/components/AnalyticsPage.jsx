// src/components/Analytics/AnalyticsPage.jsx (AŽURIRAN)

import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner, Modal, Table } from 'react-bootstrap';
import styles from '../Analytics.module.css';

// Uvoz service funkcija za ANALITIKU (za brojeve)
import {
    getAverageActiveShipmentWeight,
    bulkMarkOverdue,
    // fetchVehicleAlertStatus // Pretpostavimo da je ova funkcija definirana u AnalyticsService.js
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
    // Koristi padobran u slučaju da je num null/undefined
    const value = num !== null && num !== undefined ? num : 0;
    return value.toLocaleString('hr-HR', { maximumFractionDigits: 2 }) + unit;
};

// **NAPOMENA:** Morate u AnalyticsService.js dodati mock funkciju
// za fetchVehicleAlertStatus, ili ručno postaviti brojeve za testiranje,
// jer je ona ključna za analytics state.

// =================================================================
// 🚀 GLAVNA KOMPONENTA
// =================================================================

const AnalyticsPage = () => {

    // --- STANJE ZA AGREGIRANE VRIJEDNOSTI ---
    const [auth, setAuth] = useState(getAuthData());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [avgWeight, setAvgWeight] = useState(0);
    // ⭐ Privremeno postavljam mock brojeve dok ne dodate fetchVehicleAlertStatus
    const [analytics, setAnalytics] = useState({ overdue: 0, warning: 0, free: 0, total: 0 }); // Brojevi

    // --- STANJE ZA BULK OPERACIJE ---
    const [isLoading, setIsLoading] = useState(false);
    const [overdueMessage, setOverdueMessage] = useState('');

    // --- 🆕 STANJE ZA DETALJNE LISTE ---
    const [overdueList, setOverdueList] = useState([]);
    const [warningList, setWarningList] = useState([]);
    const [freeList, setFreeList] = useState([]);

    // --- STANJE ZA MODALE ---
    const [showModal, setShowModal] = useState(null); // 'overdue', 'warning', 'free'

    // --- UTILITY MEMO VRIJEDNOSTI ---
    const canViewAnalytics = auth.roles.includes('ROLE_ADMIN') || auth.roles.includes('ROLE_DISPATCHER');
    const isAdmin = auth.roles.includes('ROLE_ADMIN');
    const token = auth.token;


    const loadData = useCallback(async () => {
        if (!canViewAnalytics || !token) {
            setLoading(false);
            return;
        }

        try {
            // Dohvat agregiranih podataka
            // ⭐ Zbog nedostatka implementacije funkcije, ovdje je privremeno zamijenjen kod:
            // const [weight, vehicleStatus] = await Promise.all([...
            const weight = await getAverageActiveShipmentWeight(token);
            setAvgWeight(weight);
            // setAnalytics(await fetchVehicleAlertStatus(token));

            // 🆕 Dohvat detaljnih listi
            const [ovList, waList, frList] = await Promise.all([
                fetchOverdueVehicles(),
                fetchWarningVehicles(),
                fetchFreeVehiclesDetails()
            ]);

            setOverdueList(ovList);
            setWarningList(waList);
            setFreeList(frList);

            // ⭐ Ako ne koristite fetchVehicleAlertStatus, ručno postavite brojeve za kartice:
            setAnalytics({
                overdue: ovList.length,
                warning: waList.length,
                free: frList.length,
                total: ovList.length + waList.length + frList.length
                // NAPOMENA: Ovo nije točan ukupni broj vozila, ali služi za prikaz
            });


            setError(null);
        } catch (err) {
            setError(err.message || 'Greška pri dohvaćanju analitičkih podataka.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [canViewAnalytics, token]);


    useEffect(() => {
        loadData();
    }, [loadData]);


    const handleMarkOverdue = async () => {
        if (!isAdmin || isLoading) return;

        setIsLoading(true);
        setOverdueMessage('');

        try {
            const responseText = await bulkMarkOverdue(token);
            setOverdueMessage(`[SUCCESS] ${responseText}`);

            // Osvježi analitiku nakon bulk operacije
            await loadData();

        } catch (err) {
            setOverdueMessage(`[ERROR] ${err.message || 'Neuspjela masovna akcija.'}`);
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    // --- POMOĆNA KOMPONENTA ZA PRIKAZ DETALJA (MODAL) ---
    const DetailModal = ({ list, title, showKey, closeKey, columns }) => {

        // ⭐ KRITIČNA KOREKCIJA: ListToShow sada kopira ključna polja, uključujući licensePlate i remainingKmToService
        const listToShow = list.map(item => ({
            ...item, // Kopiraj sva polja
            // Dodatna proračunata polja
            name: `${item.make} ${item.model} (${item.modelYear})`,
            driver: item.currentDriver ? `${item.currentDriver.fullName}` : 'N/A',
            // Opcionalno, preostali km za maintenance
            remainingKm: item.remainingKmToService,
        }));

        return (
            <Modal show={showModal === showKey} onHide={() => setShowModal(null)} size="lg" centered>
                <Modal.Header closeButton>
                    <Modal.Title className={`font-monospace text-${closeKey === 'overdue' ? 'danger' : closeKey === 'warning' ? 'warning' : 'success'}`}>
                        {title} ({list.length})
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Table striped bordered hover size="sm" className='font-monospace'>
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Vozilo (God.)</th>
                            <th>Reg. Oznaka</th>
                            {columns.includes('driver') && <th>Trenutni Vozač</th>}
                            {/* Ispravljen naslov stupca */}
                            {columns.includes('km') && <th>Preostali Km do servisa</th>}
                        </tr>
                        </thead>
                        <tbody>
                        {listToShow.map((item, index) => (
                            <tr key={item.id}>
                                <td>{index + 1}</td>
                                <td>{item.name}</td>
                                {/* ⭐ ISPRAVLJENO: Korištenje licensePlate iz item. */}
                                <td>{item.licensePlate}</td>
                                <td className={item.driver === 'N/A' ? 'text-danger' : 'text-success'}>{item.driver}</td>

                                {/* ⭐ ISPRAVLJENO: Korištenje remainingKm iz item. */}
                                {columns.includes('km') && (
                                    <td className={item.remainingKm < 0 ? 'text-danger fw-bold' : item.remainingKm <= 5000 ? 'text-warning' : 'text-secondary'}>
                                        {item.remainingKm !== null && item.remainingKm !== undefined
                                            ? formatNumber(item.remainingKm, ' km')
                                            : 'N/A'}
                                    </td>
                                )}
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                </Modal.Body>
            </Modal>
        );
    };

    // --- KARTICE ZA VOZILA (KORISTIMO MAPIRANJE) ---
    const vehicleCards = useMemo(() => [
        {
            title: "HITNI SERVIS",
            value: analytics.overdue,
            variant: "danger",
            key: 'overdue',
            list: overdueList,
            description: "Vozila kojima je servis PREKORAČEN (km < 0)."
        },
        {
            title: "UPOZORENJE SERVIS",
            value: analytics.warning,
            variant: "warning",
            key: 'warning',
            list: warningList,
            description: `Vozila unutar praga upozorenja (npr. 0 - 5000 km).`
        },
        {
            title: "SLOBODNA VOZILA",
            value: analytics.free,
            variant: "success",
            key: 'free',
            list: freeList,
            description: "Vozila kojima NIJE dodijeljen vozač."
        },
        {
            title: "UKUPNA FLOTA",
            value: analytics.total,
            variant: "info",
            key: 'total',
            list: [], // Nema detaljne liste za Total
            description: "Ukupan broj vozila u sustavu."
        },
    ], [analytics, overdueList, warningList, freeList]);


    // =================================================================
    // 🎨 RENDER KOMPONENTE
    // =================================================================
    if (!canViewAnalytics) {
        return (
            <Container className="mt-5">
                <Alert variant="danger" className='font-monospace'>
                    Pristup odbijen. Analitika je dostupna samo ADMIN/DISPATCHER korisnicima.
                </Alert>
            </Container>
        );
    }

    return (
        <Container className="mt-5">
            <h1 className='mb-4 font-monospace'>Fleet Analytics Dashboard</h1>

            {loading && <Spinner animation="border" variant="primary" className='mb-4' />}
            {error && <Alert variant="danger" className='font-monospace'>{error}</Alert>}

            {/* Red s ANALITIČKIM KARTICAMA (Vozila) */}
            <Row xs={1} md={2} lg={4} className="g-4 mb-5">
                {vehicleCards.map((card) => (
                    <Col key={card.key}>
                        <Card border={card.variant} className={`text-center font-monospace h-100`}>
                            <Card.Header className={`bg-${card.variant} text-white fw-bold`}>{card.title}</Card.Header>
                            <Card.Body>
                                <Card.Title style={{ fontSize: '2.5rem' }}>{formatNumber(card.value)}</Card.Title>
                                <Card.Text className='text-muted'>{card.description}</Card.Text>
                                {/* Prikaz gumba za detalje samo ako postoji lista i nije Total */}
                                {card.key !== 'total' && card.list.length > 0 && (
                                    <Button
                                        variant={`outline-${card.variant}`}
                                        size="sm"
                                        onClick={() => setShowModal(card.key)}
                                        className='fw-bold mt-2 font-monospace'
                                    >
                                        Prikaži {card.list.length} Detalja
                                    </Button>
                                )}
                            </Card.Body>
                        </Card>
                    </Col>
                ))}
            </Row>

            {/* Red s ANALITIČKIM KARTICAMA (Pošiljke i Bulk) */}
            <Row xs={1} md={2} lg={4} className="g-4 mb-5">
                {/* 1. KARTICA: Prosječna težina pošiljaka */}
                <Col lg={4}>
                    <Card border="info" className={`text-center font-monospace h-100`}>
                        <Card.Header className={`bg-info text-white fw-bold`}>PROSJEČNA TEŽINA POŠILJKI</Card.Header>
                        <Card.Body>
                            <Card.Title style={{ fontSize: '2.5rem' }}>{formatNumber(avgWeight, ' kg')}</Card.Title>
                            <Card.Text className='text-muted'>Prosječna težina svih aktivnih pošiljaka (Pending/In Transit).</Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

                {/* 2. KARTICA: BULK UPDATE (Vidljivo samo Adminu) */}
                {isAdmin && (
                    <Col lg={8}>
                        <Card className={styles.terminalCard}>
                            <Card.Header className='text-success font-monospace'>
                                {'>'} ADMIN_ACTION_02_BULK_UPDATE
                            </Card.Header>
                            <Card.Body>
                                <Card.Title className='text-danger'>MASOVNA AKCIJA: MARK OVERDUE</Card.Title>
                                <Card.Text className='text-muted'>

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
                                    <span style={{ color: overdueMessage.includes('ERROR') ? '#ff4d4d' : '#90ee90' }} className='font-monospace'>
                                        {overdueMessage || 'Status: READY'}
                                    </span>
                                </p>
                            </Card.Body>
                        </Card>
                    </Col>
                )}
            </Row>

            {/* Modali za prikaz detaljnih listi */}
            {/* Ovdje koristimo listu s KRITIČNIM POLJIMA za prikaz */}
            {showModal && (
                <>
                    <DetailModal
                        list={overdueList}
                        title="Vozila KASNIMO sa Servisom"
                        showKey="overdue"
                        closeKey="overdue"
                        columns={['driver', 'km']} // Prikazuje i vozača i km
                    />
                    <DetailModal
                        list={warningList}
                        title="Vozila u Upozorenju za Servis"
                        showKey="warning"
                        closeKey="warning"
                        columns={['driver', 'km']} // Prikazuje i vozača i km
                    />
                    <DetailModal
                        list={freeList}
                        title="Detalji Slobodnih Vozila"
                        showKey="free"
                        closeKey="free"
                        columns={['driver']} // Prikazuje samo vozača (bit će N/A)
                    />
                </>
            )}

        </Container>
    );
};

export default AnalyticsPage;
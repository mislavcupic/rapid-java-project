import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
// Iako useParams i useNavigate dolaze iz react-router-dom, ostavljam ih kao dio React okruženja
// U ovom okruženju, navigacija će biti simulirana (alert)
import { useParams, useNavigate } from 'react-router-dom';

// =================================================================
// 🛑 SAMOSTALNE (MOCK) IMPLEMENTACIJE ZBOG OGRANIČENJA JEDNE DATOTEKE
// Uklonjeni importi: 'react-i18next', 'react-leaflet', 'leaflet'
// =================================================================

// 1. MOCK ZA I18N (Prevođenje)
const t = (key) => {
    const translations = {
        'shipments.tracking_label': 'Broj za praćenje',
        'shipments.status_label': 'Status',
        'shipments.origin_label': 'Polazište',
        'shipments.destination_label': 'Odredište',
        'shipments.weight_label': 'Težina (kg)',
        'shipments.departure_time_label': 'Vrijeme polaska',
        'shipments.delivery_time_label': 'Očekivani dolazak',
        'shipments.description_label': 'Opis pošiljke',
        'shipments.edit_title': 'Uredi pošiljku',
        'shipments.create_title': 'Kreiraj novu pošiljku',
        'shipments.error_load': 'Greška pri učitavanju pošiljke. Koriste se mock podaci.',
        'shipments.success_edit': 'Pošiljka uspješno ažurirana (MOCK).',
        'shipments.success_create': 'Nova pošiljka uspješno kreirana (MOCK).',
        'shipments.address_geocode_warning': 'Adresa nije geokodirana. Molimo pričekajte ili ispravite adresu.',
        'shipments.status_pending': 'Na čekanju',
        'shipments.status_assigned': 'Dodijeljeno',
        'shipments.status_in_transit': 'U tranzitu',
        'shipments.status_delivered': 'Isporučeno',
        'shipments.status_cancelled': 'Otkazano',
        'shipments.details_button': 'Detalji',
        'shipments.map_title': 'Lokacija na mapi',
        'general.loading': 'Učitavanje...',
        'general.save_changes': 'Spremi promjene',
        'general.cancel': 'Odustani',
        'shipments.create_button': 'Kreiraj Pošiljku',
        'shipments.error_general': 'Došlo je do opće greške prilikom spremanja.',
    };
    return translations[key] || key;
};

// 2. MOCK ZA API SERVISE
const MOCK_SHIPMENT_DATA = {
    trackingNumber: 'TRK12345',
    originAddress: 'Avenija Dubrovnik 15, Zagreb',
    destinationAddress: 'Trg bana Josipa Jelačića 1, Zagreb',
    status: 'IN_TRANSIT',
    weightKg: 5.5,
    departureTime: new Date(Date.now() - 86400000).toISOString(),
    expectedDeliveryDate: new Date(Date.now() + 86400000).toISOString(),
    description: 'Hitna dokumentacija',
    originLatitude: 45.7772,
    originLongitude: 15.9757,
    destinationLatitude: 45.8129,
    destinationLongitude: 15.9770,
};

const fetchShipmentById = async (id) => {
    await new Promise(resolve => setTimeout(resolve, 500));
    if (id) return MOCK_SHIPMENT_DATA;
    return null;
};
const createShipment = async (data) => {
    await new Promise(resolve => setTimeout(resolve, 500));
    console.log('Mock Create Shipment:', data);
    return { id: 'NEW123', ...data };
};
const updateShipment = async (id, data) => {
    await new Promise(resolve => setTimeout(resolve, 500));
    console.log(`Mock Update Shipment ${id}:`, data);
};
const geocodeAddress = async (address) => {
    await new Promise(resolve => setTimeout(resolve, 300));
    if (address.toLowerCase().includes('zagreb') && address.toLowerCase().includes('dubrovnik')) return { lat: 45.7772, lng: 15.9757 };
    if (address.toLowerCase().includes('zagreb') && address.toLowerCase().includes('jelačić')) return { lat: 45.8129, lng: 15.9770 };
    if (address.toLowerCase().includes('rijeka')) return { lat: 45.3271, lng: 14.4422 };
    return null;
};


// =================================================================
// 🛑 KOMPLETAN LEAFLET KOD (IN-LINE)
// Koristimo CDN uvoz, ali moramo osigurati da React ne pokušava riješiti import
// Stoga ćemo sve Leaflet objekte i komponente generirati SAMOSTALNO.
// Budući da react-leaflet NE MOŽE biti uvezen, zamijenit ćemo MapContainer
// jednostavnom HTML div komponentom.
// =================================================================

// 3. INLINED CSS ZA LEAFLET (Zamjenjuje 'leaflet/dist/leaflet.css')
const LEAFLET_STYLE = `
.leaflet-container {
    height: 100%;
    width: 100%;
    border-radius: 0.5rem;
}
.map-placeholder {
    height: 400px;
    width: 100%;
    background-color: #f8f9fa;
    border: 1px solid #dee2e6;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6c757d;
    font-family: monospace;
    text-align: center;
    border-radius: 0.5rem;
}
`;
// 4. BASE64 IKONA ZA LEAFLET (Isto kao prije)
const LEAFLET_MARKER_BASE64 = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAzODQgNTEyIj48cGF0aCBmaWxsPSJyZWQiIGQ9Ik0xNzIuMSA0MjEuMzY3QzE3Mi4xIDQyMS43ODkgMjAzIDUxMiAyMDMgNTEyQzIwMyA1MTIgMjMzLjkgNDIxLjc4OSAyMzMuOSA0MjEuMzY3QzI2NS42OTUgMzEzLjIyNyAzNzMgMjYzLjA0NSAzNzMgMTc1LjY0NEMzNzMgNzguNDg4IDI5OC4yNDYgMCAxOTkgMCAxMDAuNzU0IDAgMjYgNzguNDg4IDI2IDE3NS42NDRDMjYgMjYzLjA0NSAxMzMuMzI1IDMxMy4yMjcgMTY1LjA5NSA0MjEuMzY3TDE3Mi4xIDQyMS4zNjdNMjIwLjQgMjM0LjIzNUSyMjAuNCAyNjguODg3IDE5OSAyNjguODg3IDE5OSAyNjguODg3QzE3Ny42IDI2OC44ODcgMTU2LjIgMjI3LjA5MSAxNTYuMiAxOTkuNjM5QzE1Ni4yIDE3Mi4xODcgMTc3LjYgMTQ3Ljc4NiAxOTkgMTQ3Ljc4NkN0MjAuNCAxNDcuNzg2IDIyMC40IDE3Mi4xODcgMjIwLjQgMjI3LjA5MVYyMzQuMjM1WiIvPjwvc3ZnPg==';

/**
 * Komponenta Placeholder za Mapu
 * Budući da ne možemo uvesti react-leaflet, zamjenjujemo je vizualnim placeholderom.
 */
const MapPlaceholder = React.memo(({ origin, destination }) => {
    // Prikazujemo koordinate unutar placeholder-a
    const hasOrigin = origin.lat !== 0;
    const hasDestination = destination.lat !== 0;

    return (
        <div className="map-placeholder">
            <div>
                <p className="fw-bold">Prikaz mape je onemogućen</p>
                <small>(Zbog ograničenja okruženja, vanjska biblioteka "react-leaflet" ne može biti učitana)</small>
                {hasOrigin && (
                    <p className="mt-3 mb-0">Polazište: {origin.lat.toFixed(4)}, {origin.lng.toFixed(4)}</p>
                )}
                {hasDestination && (
                    <p>Odredište: {destination.lat.toFixed(4)}, {destination.lng.toFixed(4)}</p>
                )}
                {!hasOrigin && !hasDestination && (
                    <p className="mt-3">Unesite adrese za prikaz koordinata.</p>
                )}
            </div>
        </div>
    );
});
MapPlaceholder.displayName = 'MapPlaceholder';


// Funkcija za formatiranje datuma/vremena
const formatDateTimeLocal = (isoString) => {
    if (!isoString) {
        return new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    }
    return isoString.slice(0, 16);
}

// Funkcija za debouncing
const debounce = (func, delay) => {
    let timeoutId;
    return (...args) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            func.apply(null, args);
        }, delay);
    };
};

// =================================================================
// GLAVNA KOMPONENTA
// =================================================================

const ShipmentForm = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        status: 'PENDING',
        weightKg: 0,
        departureTime: formatDateTimeLocal(null),
        expectedDeliveryDate: formatDateTimeLocal(null),
        description: '',
        originLatitude: 0,
        originLongitude: 0,
        destinationLatitude: 0,
        destinationLongitude: 0,
    });

    const [originCoords, setOriginCoords] = useState({ lat: 0, lng: 0 });
    const [destinationCoords, setDestinationCoords] = useState({ lat: 0, lng: 0 });
    const [geocodeLoading, setGeocodeLoading] = useState(false);

    // =================================================================
    // Geocoding logika
    // =================================================================

    const geocodeHandler = useCallback(async (address, setCoords, setFormStateUpdater) => {
        if (!address) {
            setCoords({ lat: 0, lng: 0 });
            setFormStateUpdater({ lat: 0, lng: 0});
            return;
        }
        setGeocodeLoading(true);
        try {
            const coords = await geocodeAddress(address);
            if (coords) {
                setCoords(coords);
                setFormStateUpdater(coords);
            } else {
                setCoords({ lat: 0, lng: 0 });
                setFormStateUpdater({ lat: 0, lng: 0 });
            }
        } catch (err) {
            console.error("Geocoding failed:", err);
            setCoords({ lat: 0, lng: 0 });
            setFormStateUpdater({ lat: 0, lng: 0 });
        } finally {
            setGeocodeLoading(false);
        }
    }, []);

    const debouncedGeocodeOrigin = useMemo(() => debounce((address) => {
        geocodeHandler(
            address,
            setOriginCoords,
            (coords) => setFormData(prev => ({
                ...prev,
                originLatitude: coords.lat,
                originLongitude: coords.lng
            }))
        );
    }, 1000), [geocodeHandler]);

    const debouncedGeocodeDestination = useMemo(() => debounce((address) => {
        geocodeHandler(
            address,
            setDestinationCoords,
            (coords) => setFormData(prev => ({
                ...prev,
                destinationLatitude: coords.lat,
                destinationLongitude: coords.lng
            }))
        );
    }, 1000), [geocodeHandler]);


    // Učitavanje pošiljke
    useEffect(() => {
        const loadShipment = async () => {
            if (!isEditMode) {
                setLoading(false);
                return;
            }

            try {
                const data = await fetchShipmentById(id);

                if (data) {
                    const newFormData = {
                        trackingNumber: data.trackingNumber || '',
                        originAddress: data.originAddress || '',
                        destinationAddress: data.destinationAddress || '',
                        status: data.status || 'PENDING',
                        weightKg: data.weightKg || 0,
                        departureTime: formatDateTimeLocal(data.departureTime),
                        expectedDeliveryDate: formatDateTimeLocal(data.expectedDeliveryDate),
                        description: data.description || '',
                        originLatitude: data.originLatitude || 0,
                        originLongitude: data.originLongitude || 0,
                        destinationLatitude: data.destinationLatitude || 0,
                        destinationLongitude: data.destinationLongitude || 0,
                    };

                    setFormData(newFormData);
                    setOriginCoords({ lat: data.originLatitude || 0, lng: data.originLongitude || 0 });
                    setDestinationCoords({ lat: data.destinationLatitude || 0, lng: data.destinationLongitude || 0 });
                } else {
                    setError(t('shipments.error_load'));
                }

            } catch (err) {
                setError(t('shipments.error_load'));
            } finally {
                setLoading(false);
            }
        };

        loadShipment();
    }, [id, isEditMode]);

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));

        if (name === 'originAddress') {
            debouncedGeocodeOrigin(value);
        } else if (name === 'destinationAddress') {
            debouncedGeocodeDestination(value);
        }
    }, [debouncedGeocodeOrigin, debouncedGeocodeDestination]);


    const handleSubmit = useCallback(async (e) => {
        if (e) e.preventDefault();

        setError(null);
        setSuccess(null);
        setSaving(true);

        try {
            // Provjera da li su adrese geokodirane prije slanja
            if ((formData.originAddress && !formData.originLatitude) || (formData.destinationAddress && !formData.destinationLatitude)) {
                setError(t('shipments.address_geocode_warning'));
                setSaving(false);
                return;
            }

            const shipmentData = {
                ...formData,
                weightKg: Number.parseFloat(formData.weightKg),
                // Koordinate se uvijek tretiraju kao brojevi
                originLatitude: Number.parseFloat(formData.originLatitude),
                originLongitude: Number.parseFloat(formData.originLongitude),
                destinationLatitude: Number.parseFloat(formData.destinationLatitude),
                destinationLongitude: Number.parseFloat(formData.destinationLongitude),
            };

            if (isEditMode) {
                await updateShipment(id, shipmentData);
                setSuccess(t('shipments.success_edit'));
            } else {
                await createShipment(shipmentData);
                setSuccess(t('shipments.success_create'));

                // Očisti formu nakon uspješnog kreiranja
                setFormData({
                    trackingNumber: '',
                    originAddress: '',
                    destinationAddress: '',
                    status: 'PENDING',
                    weightKg: 0,
                    departureTime: formatDateTimeLocal(null),
                    expectedDeliveryDate: formatDateTimeLocal(null),
                    description: '',
                    originLatitude: 0,
                    originLongitude: 0,
                    destinationLatitude: 0,
                    destinationLongitude: 0,
                });
                setOriginCoords({ lat: 0, lng: 0 });
                setDestinationCoords({ lat: 0, lng: 0 });
            }

            if (isEditMode) {
                // Mock navigacija
                alert(`Mock: Navigacija na listu pošiljaka s porukom: ${t('shipments.success_edit')}`);
                // navigate('/shipments', { state: { message: t('shipments.success_edit') } });
            }
        } catch (err) {
            setError(err.message || t('shipments.error_general'));
        } finally {
            setSaving(false);
        }
    }, [formData, isEditMode, id, navigate]);


    // Nema više potrebe za mapProps jer nema react-leaflet
    // const mapProps = useMemo(() => { ... }, [originCoords, destinationCoords]);


    if (loading) {
        return (
            <Container className="my-5 d-flex justify-content-center">
                {/* Ugrađeni stilovi za React Bootstrap Spinner */}
                <style>{`
                    .spinner-border-info { border-color: #17a2b8; border-right-color: transparent; }
                `}</style>
                <Spinner animation="border" variant="info" className="spinner-border-info" />
            </Container>
        );
    }

    return (
        <Container className="my-5">
            {/* Ugrađeni stilovi za Leaflet mapu Placeholder */}
            <style>{LEAFLET_STYLE}</style>

            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace mb-4">
                        {isEditMode ? t('shipments.edit_title') : t('shipments.create_title')}
                    </h2>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}


                    <Form id="shipment-form" onSubmit={handleSubmit}>
                        {/* 1. BROJ ZA PRAĆENJE i STATUS */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="trackingNumber" label={t('shipments.tracking_label')}>
                                    <Form.Control
                                        type="text"
                                        name="trackingNumber"
                                        value={formData.trackingNumber}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={50}
                                    />
                                </FloatingLabel>
                            </Col>
                            <Col md={6}>
                                <FloatingLabel controlId="status" label={t('shipments.status_label')}>
                                    <Form.Select
                                        name="status"
                                        value={formData.status}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        disabled={isEditMode && formData.status === 'ASSIGNED'}
                                    >
                                        <option value="PENDING">{t('shipments.status_pending')}</option>
                                        <option value="ASSIGNED" disabled={!isEditMode || formData.status !== 'ASSIGNED'}>
                                            {t('shipments.status_assigned')}
                                        </option>
                                        <option value="IN_TRANSIT">{t('shipments.status_in_transit')}</option>
                                        <option value="DELIVERED">{t('shipments.status_delivered')}</option>
                                        <option value="CANCELLED">{t('shipments.status_cancelled')}</option>
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>


                        {/* 2. POLAZIŠTE i ODREDIŠTE */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="originAddress" label={t('shipments.origin_label') + (geocodeLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Control
                                        type="text"
                                        name="originAddress"
                                        value={formData.originAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={100}
                                    />
                                </FloatingLabel>
                                {originCoords.lat !== 0 && (
                                    <Form.Text muted className="ms-2 font-monospace">
                                        Koordinate: {originCoords.lat.toFixed(4)}, {originCoords.lng.toFixed(4)}
                                    </Form.Text>
                                )}
                            </Col>
                            <Col md={6}>
                                <FloatingLabel controlId="destinationAddress" label={t('shipments.destination_label') + (geocodeLoading ? ' (' + t('general.loading') + ')' : '')}>
                                    <Form.Control
                                        type="text"
                                        name="destinationAddress"
                                        value={formData.destinationAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        maxLength={100}
                                    />
                                </FloatingLabel>
                                {destinationCoords.lat !== 0 && (
                                    <Form.Text muted className="ms-2 font-monospace">
                                        Koordinate: {destinationCoords.lat.toFixed(4)}, {destinationCoords.lng.toFixed(4)}
                                    </Form.Text>
                                )}
                            </Col>
                        </Row>

                        {/* 3. TEŽINA i DATUMI */}
                        <Row className="mb-4">
                            <Col md={4}>
                                <FloatingLabel controlId="weightKg" label={t('shipments.weight_label')}>
                                    <Form.Control
                                        type="number"
                                        name="weightKg"
                                        value={formData.weightKg}
                                        onChange={handleChange}
                                        required
                                        min="1"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* POLJE: Datum Polaska */}
                            <Col md={4}>
                                <FloatingLabel controlId="departureTime" label={t('shipments.departure_time_label')}>
                                    <Form.Control
                                        type="datetime-local"
                                        name="departureTime"
                                        value={formData.departureTime}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            {/* POLJE: expectedDeliveryDate */}
                            <Col md={4}>
                                <FloatingLabel controlId="expectedDeliveryDate" label={t('shipments.delivery_time_label')}>
                                    <Form.Control
                                        type="datetime-local"
                                        name="expectedDeliveryDate"
                                        value={formData.expectedDeliveryDate}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* 4. SKRIVENA POLJA ZA KOORDINATE */}
                        <Form.Control type="hidden" name="originLatitude" value={formData.originLatitude} />
                        <Form.Control type="hidden" name="originLongitude" value={formData.originLongitude} />
                        <Form.Control type="hidden" name="destinationLatitude" value={formData.destinationLatitude} />
                        <Form.Control type="hidden" name="destinationLongitude" value={formData.destinationLongitude} />


                        {/* 5. OPIS */}
                        <Row className="mb-4">
                            <Col>
                                <FloatingLabel controlId="description" label={t('shipments.description_label')}>
                                    <Form.Control
                                        as="textarea"
                                        name="description"
                                        value={formData.description}
                                        onChange={handleChange}
                                        className="font-monospace"
                                        style={{ height: '100px' }}
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>


                    </Form>

                    {/* GRUPA GUMBA ZA AKCIJU (Uključuje Detalji gumb) */}

                    {isEditMode ? (
                        // EDIT MODE: Prikaz Detalji i Spremi
                        <div className="d-grid gap-2 d-md-flex justify-content-md-between mt-3">
                            {/* Gumb 1: Detalji (PRIKAZUJE SE SAMO U EDIT MODU) */}
                            <Button
                                variant="outline-info"
                                className="fw-bold font-monospace flex-grow-1 me-md-2"
                                // Mock navigacija
                                onClick={() => alert(`Mock: Navigacija na detalje pošiljke ID: ${id}`)}
                            >
                                {t('shipments.details_button') || 'Detalji'}
                            </Button>

                            {/* Gumb 2: Spremi promjene */}
                            <Button
                                onClick={handleSubmit}
                                variant="outline-primary"
                                className="fw-bold font-monospace flex-grow-1"
                                disabled={saving}
                            >
                                {saving ? (
                                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                                ) : (
                                    t("general.save_changes") || 'Spremi promjene'
                                )}
                            </Button>
                        </div>
                    ) : (
                        // CREATE MODE: Prikaz samo Kreiraj
                        <Button
                            onClick={handleSubmit}
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace mt-3"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm"  aria-hidden="true" className="me-2" />
                            ) : (
                                t('shipments.create_button') || 'Kreiraj Pošiljku'
                            )}
                        </Button>
                    )}

                    {/* Gumb za Odustani (Uvijek prisutan) */}
                    <Button
                        variant="outline-secondary"
                        className="w-100 fw-bold font-monospace mt-2 mb-4"
                        // Mock navigacija
                        onClick={() => alert('Mock: Navigacija na listu pošiljaka /shipments')}
                    >
                        {t('general.cancel') || 'Odustani'}
                    </Button>


                    {/* MAPA (PRIKAZ) */}
                    <hr className="my-4 border-info" />
                    <div className="p-3 border rounded shadow-sm">
                        <h5 className="text-dark fw-bold font-monospace mb-3">{t('shipments.map_title')}</h5>

                        {/* ZAMJENA ZA MAPCONTAINER */}
                        <MapPlaceholder origin={originCoords} destination={destinationCoords} />
                    </div>

                </Card.Body>
            </Card>
        </Container>
    );
};

export default ShipmentForm;
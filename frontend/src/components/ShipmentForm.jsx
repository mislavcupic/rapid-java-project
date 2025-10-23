// frontend/src/components/ShipmentForm.jsx - S INTEGRIRANIM NOMINATIM GEOCORDINGOM

import React, { useState, useEffect, useCallback } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchShipmentById, createShipment, updateShipment, geocodeAddress } from '../services/ShipmentApi';
import { useTranslation } from 'react-i18next';


// Pomoćna funkcija za formatiranje datuma (LocalDateTime)
const formatDateTimeLocal = (isoString) => {
    if (!isoString) {
        return new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    }
    return isoString.slice(0, 16);
}

// 💥 Debounce funkcija - Ključna za poštivanje Nominatim pravila (1 zahtjev u sekundi)
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

const ShipmentForm = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [success, setSuccess] = useState(null);
    const [formData, setFormData] = useState({
        trackingNumber: '',
        originAddress: '',
        destinationAddress: '',
        description: '',
        status: 'PENDING',
        expectedDeliveryDate: formatDateTimeLocal(null),
        weightKg: '',
        shipmentValue: '',
        volumeM3: '',

        // Koordinate su null dok ih Nominatim ne popuni
        originLatitude: null,
        originLongitude: null,
        destinationLatitude: null,
        destinationLongitude: null,
    });

    const SHIPMENT_STATUSES = ['PENDING', 'SCHEDULED', 'IN_TRANSIT', 'DELIVERED', 'CANCELED'];

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // NOVO: STATE ZA PRIKAZ STATUSA/GREŠKE GEOCORDINGA KORISNIKU
    const [geocodeInfo, setGeocodeInfo] = useState({
        origin: 'Unesite adresu polazišta.',
        destination: 'Unesite adresu odredišta.'
    });


    // Geocoding funkcija omotana Debounce-om
    const runGeocode = useCallback(debounce(async (address, fieldPrefix) => {
        // Ne šaljemo zahtjev ako je adresa prekratka ili prazna
        if (!address || address.length < 5) {
            setGeocodeInfo(prev => ({ ...prev, [fieldPrefix]: 'Unos mora biti dulji od 5 znakova.' }));
            // Brišemo koordinate ako je adresa obrisana
            setFormData(prev => ({
                ...prev,
                [`${fieldPrefix}Latitude`]: null,
                [`${fieldPrefix}Longitude`]: null,
            }));
            return;
        }

        setGeocodeInfo(prev => ({ ...prev, [fieldPrefix]: 'Tražim koordinate... (molimo pričekajte 0.5s)' }));

        const coords = await geocodeAddress(address);

        if (coords) {
            setFormData(prev => ({
                ...prev,
                [`${fieldPrefix}Latitude`]: coords.lat,
                [`${fieldPrefix}Longitude`]: coords.lng,
            }));
            // Prikaz korisniku da su koordinate pronađene
            setGeocodeInfo(prev => ({ ...prev, [fieldPrefix]: `Pronađeno: Lat ${coords.lat.toFixed(4)}, Lng ${coords.lng.toFixed(4)}` }));
        } else {
            // Ako nije pronađeno, koordinate su null (što trigerira validaciju kod spremanja)
            setFormData(prev => ({
                ...prev,
                [`${fieldPrefix}Latitude`]: null,
                [`${fieldPrefix}Longitude`]: null,
            }));
            setGeocodeInfo(prev => ({ ...prev, [fieldPrefix]: 'Greška: Koordinate nisu pronađene za ovu adresu.' }));
        }
    }, 500), []); // 500ms debounce delay


    // Učitavanje pošiljke (ostaje isto, samo učitava i koordinate)
    useEffect(() => {
        const loadShipment = async () => {
            if (!localStorage.getItem('accessToken')) {
                setError("Molimo, prijavite se za pristup formi.");
                setLoading(false);
                return;
            }

            if (isEditMode) {
                try {
                    const data = await fetchShipmentById(id);
                    setFormData({
                        trackingNumber: data.trackingNumber || '',
                        originAddress: data.originAddress || '',
                        destinationAddress: data.destinationAddress || '',
                        description: data.description || '',
                        status: data.status || 'PENDING',
                        expectedDeliveryDate: formatDateTimeLocal(data.expectedDeliveryDate),
                        weightKg: data.weightKg || '',
                        shipmentValue: data.shipmentValue || '',
                        volumeM3: data.volumeM3 || '',
                        // Učitavanje koordinata
                        originLatitude: data.originLatitude || null,
                        originLongitude: data.originLongitude || null,
                        destinationLatitude: data.destinationLatitude || null,
                        destinationLongitude: data.destinationLongitude || null,
                    });
                    // Nakon učitavanja, postavi info o koordinatama
                    if (data.originLatitude && data.destinationLatitude) {
                        setGeocodeInfo({
                            origin: `Učitano: Lat ${data.originLatitude.toFixed(4)}, Lng ${data.originLongitude.toFixed(4)}`,
                            destination: `Učitano: Lat ${data.destinationLatitude.toFixed(4)}, Lng ${data.destinationLongitude.toFixed(4)}`,
                        });
                    }
                    setError(null);
                } catch (err) {
                    console.error("Greška pri učitavanju pošiljke:", err);
                    setError(err.message || "Greška pri učitavanju pošiljke.");
                }
            }
            setLoading(false);
        };

        loadShipment();
    }, [id, isEditMode]);


    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({ ...prev, [name]: value }));

        // 💥 POZIV GEOCORDINGA S DEBOUNCE-OM
        if (name === 'originAddress') {
            runGeocode(value, 'origin');
        } else if (name === 'destinationAddress') {
            runGeocode(value, 'destination');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        // ... (Provjera obaveznih polja)
        if (!formData.trackingNumber || !formData.originAddress || !formData.destinationAddress ||
            !formData.expectedDeliveryDate || !formData.weightKg) {

            setError('Molimo popunite sva obavezna polja (Broj za praćenje, Adrese, Datum i Težina).');
            setSaving(false);
            return;
        }

        // ✅ KRITIČNA VALIDACIJA: Koordinate su obavezne (Back-end zahtjev)
        if (!formData.originLatitude || !formData.destinationLatitude ||
            !formData.originLongitude || !formData.destinationLongitude) {

            // Poruka koja usmjerava korisnika
            setError('Koordinate polazišta i odredišta su obavezne! Molimo pričekajte da se adrese geokodiraju (zelena poruka ispod polja).');
            setSaving(false);
            return;
        }

        // ČIŠĆENJE I KONVERZIJA PODATAKA
        const dataToSend = { ...formData };
        for (const key in dataToSend) {
            const value = dataToSend[key];
            if (typeof value === 'string' && value.trim() === '') {
                dataToSend[key] = null;
            }
        }

        dataToSend.weightKg = dataToSend.weightKg ? Number(dataToSend.weightKg) : null;
        dataToSend.volumeM3 = dataToSend.volumeM3 ? Number(dataToSend.volumeM3) : null;
        dataToSend.shipmentValue = dataToSend.shipmentValue ? Number(dataToSend.shipmentValue) : null;


        try {
            if (id) {
                await updateShipment(id, dataToSend);
                setSuccess(t('messages.shipment_updated'));
            } else {
                await createShipment(dataToSend);
                setSuccess(t('messages.shipment_created'));
            }
            setTimeout(() => navigate('/shipments'), 1500);
        } catch (err) {
            console.error("Greška pri spremanju pošiljke:", err);
            setError(err.message || 'Greška pri spremanju pošiljke. Provjerite podatke i pokušajte ponovno.');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" role="status" />
                <p className="text-muted mt-2">{t("general.loading_form")}</p>
            </div>
        );
    }

    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-primary border-top-0 border-5" style={{ maxWidth: '800px' }}>
                <Card.Header className="bg-primary text-white">
                    <h1 className="h4 mb-0 font-monospace">{isEditMode ? `Uredi Pošiljku (Tracking: ${formData.trackingNumber})` : 'Kreiraj Novu Pošiljku'}</h1>
                </Card.Header>
                <Card.Body>

                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit} className="p-1">

                        <Row className="mb-3">
                            <Col md={6}>
                                <FloatingLabel controlId="formTrackingNumber" label="Broj za praćenje (Tracking No.)">
                                    <Form.Control
                                        type="text"
                                        name="trackingNumber"
                                        value={formData.trackingNumber}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            <Col md={6}>
                                <FloatingLabel controlId="formStatus" label={t("assignments.status")}>
                                    <Form.Select
                                        name="status"
                                        value={formData.status}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                        disabled={isEditMode && formData.status !== 'PENDING' && formData.status !== 'SCHEDULED'}
                                    >
                                        {SHIPMENT_STATUSES.map(s => (
                                            <option key={s} value={s}>{s}</option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </Col>
                        </Row>

                        <Row className="mb-3">
                            {/* Polazište */}
                            <Col md={6}>
                                <FloatingLabel controlId="formOriginAddress" label="Adresa Polazišta">
                                    <Form.Control
                                        type="text"
                                        name="originAddress"
                                        value={formData.originAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                                {/* Poruka za Geocoding status (koordinate) */}
                                <Form.Text
                                    className={`fw-bold ms-2 ${
                                        geocodeInfo.origin.includes('Pronađeno') ? 'text-success' :
                                            geocodeInfo.origin.includes('Greška') ? 'text-danger' :
                                                'text-muted'
                                    }`}
                                >
                                    {geocodeInfo.origin}
                                </Form.Text>
                            </Col>

                            {/* Odredište */}
                            <Col md={6}>
                                <FloatingLabel controlId="formDestinationAddress" label={t("shipments.destination")}>
                                    <Form.Control
                                        type="text"
                                        name="destinationAddress"
                                        value={formData.destinationAddress}
                                        onChange={handleChange}
                                        required
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                                {/* Poruka za Geocoding status (koordinate) */}
                                <Form.Text
                                    className={`fw-bold ms-2 ${
                                        geocodeInfo.destination.includes('Pronađeno') ? 'text-success' :
                                            geocodeInfo.destination.includes('Greška') ? 'text-danger' :
                                                'text-muted'
                                    }`}
                                >
                                    {geocodeInfo.destination}
                                </Form.Text>
                            </Col>
                        </Row>

                        {/* SKRIVENA POLJA ZA KOORDINATE - Automatski popunjena Nominatimom */}
                        <Form.Control type="hidden" name="originLatitude" value={formData.originLatitude || ''} />
                        <Form.Control type="hidden" name="originLongitude" value={formData.originLongitude || ''} />
                        <Form.Control type="hidden" name="destinationLatitude" value={formData.destinationLatitude || ''} />
                        <Form.Control type="hidden" name="destinationLongitude" value={formData.destinationLongitude || ''} />


                        {/* Datum Isporuke i Težina - OSTAJE ISTO */}
                        <Row className="mb-3">
                            <Col md={6}>
                                <FloatingLabel controlId="expectedDeliveryDate" label="Očekivani Datum Isporuke">
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

                            <Col md={6}>
                                <FloatingLabel controlId="weightKg" label={t("shipments.weight")}>
                                    <Form.Control
                                        type="number"
                                        name="weightKg"
                                        value={formData.weightKg}
                                        onChange={handleChange}
                                        required
                                        min="0.1"
                                        step="0.01"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>

                        {/* Vrijednost i Volumen - OSTAJE ISTO */}
                        <Row className="mb-4">
                            <Col md={6}>
                                <FloatingLabel controlId="shipmentValue" label="Vrijednost (€)">
                                    <Form.Control
                                        type="number"
                                        name="shipmentValue"
                                        value={formData.shipmentValue}
                                        onChange={handleChange}
                                        min="0"
                                        step="0.01"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>

                            <Col md={6}>
                                <FloatingLabel controlId="volumeM3" label="Volumen (m³)">
                                    <Form.Control
                                        type="number"
                                        name="volumeM3"
                                        value={formData.volumeM3}
                                        onChange={handleChange}
                                        min="0"
                                        step="0.01"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </Col>
                        </Row>


                        {/* Opis - OSTAJE ISTO */}
                        <Row className="mb-4">
                            <Col>
                                <FloatingLabel controlId="formDescription" label="Opis Pošiljke (Opcionalno)">
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

                        <Button
                            type="submit"
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                isEditMode ? t("assignments.edit_button") : 'Kreiraj Pošiljku'
                            )}
                        </Button>
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/shipments')}
                        >
                            Odustani
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ShipmentForm;
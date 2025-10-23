// frontend/src/components/DriverForm.jsx

import React, { useState, useEffect } from 'react';
import { Form, Card, Button, Container, Row, Col, Alert, FloatingLabel, Spinner } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchDriverById, createDriver, updateDriver } from '../services/DriverApi';
import { useTranslation } from 'react-i18next';

const DriverForm = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = !!id;

    const [formData, setFormData] = useState({
        // Driver polja
        licenseNumber: '',
        phoneNumber: '',
        licenseExpirationDate: '',

        // UserInfo polja za kreiranje
        username: '',
        password: '',
        firstName: '',
        lastName: '',
        email: '', // ✅ DODANO EMAIL POLJE
    });

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [success, setSuccess] = useState(null);

    // Učitavanje podataka za uređivanje
    useEffect(() => {
        const loadFormData = async () => {
            try {
                if (isEditMode) {
                    const data = await fetchDriverById(id);
                    setFormData({
                        licenseNumber: data.licenseNumber || '',
                        phoneNumber: data.phoneNumber || '',
                        licenseExpirationDate: data.licenseExpirationDate || '',

                        // Podaci se dohvaćaju iz DTO-a za prikaz u edit modu
                        username: data.username || '',
                        firstName: data.firstName || '',
                        lastName: data.lastName || '',
                        email: data.email || '', // ✅ Dohvaćanje emaila
                        password: '',
                    });
                }
            } catch (err) {
                setError(err.message || "Greška pri učitavanju podataka.");
            } finally {
                setLoading(false);
            }
        };
        loadFormData();
    }, [id, isEditMode]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        // Klijentska validacija za Driver polja
        if (!formData.licenseExpirationDate || !formData.licenseNumber || !formData.phoneNumber) {
            setError(t('messages.all_driver_fields_required'));
            setSaving(false);
            return;
        }

        // Dodatna validacija za KREIRANJE novog korisnika
        if (!isEditMode && (!formData.username || !formData.password || !formData.firstName || !formData.lastName || !formData.email)) {
            setError(t('messages.driver_creation_fields_required'));
            setSaving(false);
            return;
        }

        // Osnovni Driver podaci (potrebni za oba moda)
        const baseData = {
            licenseNumber: formData.licenseNumber,
            licenseExpirationDate: formData.licenseExpirationDate,
            phoneNumber: formData.phoneNumber,
        };

        let dataToSubmit = baseData;

        // Pri KREIRANJU dodajemo UserInfo polja
        if (!isEditMode) {
            dataToSubmit = {
                ...baseData,
                username: formData.username,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email, // ✅ SLANJE EMAILA BACKENDU
            };
        }

        try {
            if (isEditMode) {
                // UPDATE šalje samo baseData
                await updateDriver(id, baseData);
                setSuccess(t('messages.driver_updated'));
            } else {
                // CREATE šalje sva polja
                await createDriver(dataToSubmit);
                setSuccess('Novi vozač i korisnički račun uspješno kreirani!');
            }
            setTimeout(() => navigate('/drivers'), 1500);
        } catch (err) {
            setError(err.message || "Spremanje nije uspjelo.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) return (
        <Container className="pt-5 text-center"><Spinner animation="border" variant="success" /><p className="mt-2">Učitavam...</p></Container>
    );

    return (
        <Container className="d-flex justify-content-center pt-3">
            <Card className="shadow-lg w-100 border-success border-top-0 border-5" style={{ maxWidth: '600px' }}>
                <Card.Header className="bg-success text-white">
                    <h1 className="h4 mb-0 font-monospace">{isEditMode ? `Uredi Vozača: ${formData.username}` : 'Kreiraj Novog Vozača'}</h1>
                </Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit}>


                        {/* ------------------------------------------- */}
                        {/* POLJA ZA USERINFO (SAMO PRI KREIRANJU) */}
                        {/* ------------------------------------------- */}
                        {!isEditMode && (
                            <>
                                <p className="text-muted fw-bold">Podaci za Kreiranje Korisnika (Automatski ROLE_DRIVER)</p>

                                <Row>
                                    <Col md={6}>
                                        <FloatingLabel controlId="firstName" label="Ime" className="mb-3">
                                            <Form.Control type="text" name="firstName" value={formData.firstName} onChange={handleChange} required className="font-monospace" />
                                        </FloatingLabel>
                                    </Col>
                                    <Col md={6}>
                                        <FloatingLabel controlId="lastName" label={t("forms.lastName")} className="mb-3">
                                            <Form.Control type="text" name="lastName" value={formData.lastName} onChange={handleChange} required className="font-monospace" />
                                        </FloatingLabel>
                                    </Col>
                                </Row>

                                <FloatingLabel controlId="username" label="Korisničko Ime (Username)" className="mb-3">
                                    <Form.Control type="text" name="username" value={formData.username} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>

                                <FloatingLabel controlId="email" label={t("forms.email")} className="mb-3">
                                    <Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>

                                <FloatingLabel controlId="password" label="Lozinka (za kreiranje)" className="mb-4">
                                    <Form.Control type="password" name="password" value={formData.password} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                                <hr className="my-4"/>
                            </>
                        )}

                        {/* Prikaz Imena/Prezimena/Username-a/Email-a u edit modu */}
                        {isEditMode && (
                            <>
                                <Row className='mb-3'>
                                    <Col>
                                        <p className="text-muted mb-0">Ime i Prezime: <span className="fw-bold">{formData.firstName} {formData.lastName}</span></p>
                                    </Col>
                                    <Col className="text-end">
                                        <p className="text-muted mb-0">Username: <span className="fw-bold">{formData.username}</span></p>
                                    </Col>
                                </Row>
                                <p className="text-muted mb-4">E-mail: <span className="fw-bold">{formData.email}</span></p>
                                <hr className="my-4"/>
                            </>
                        )}


                        {/* ------------------------------------------- */}
                        {/* POLJA ZA DRIVER ENTITET */}
                        {/* ------------------------------------------- */}

                        {/* 1. Broj licence */}
                        <FloatingLabel controlId="licenseNumber" label={t("forms.license_number")} className="mb-3">
                            <Form.Control type="text" name="licenseNumber" value={formData.licenseNumber} onChange={handleChange} required className="font-monospace" />
                        </FloatingLabel>

                        {/* 2. Telefon */}
                        <FloatingLabel controlId="phoneNumber" label={t("forms.phone")} className="mb-3">
                            <Form.Control type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} className="font-monospace" required />
                        </FloatingLabel>

                        {/* 3. Datum isteka licence */}
                        <FloatingLabel controlId="licenseExpirationDate" label="Datum Isteka Licence (YYYY-MM-DD)" className="mb-4">
                            <Form.Control
                                type="date"
                                name="licenseExpirationDate"
                                value={formData.licenseExpirationDate}
                                onChange={handleChange}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>


                        <Button type="submit" variant="outline-success" className="w-100 fw-bold font-monospace" disabled={saving}>
                            {saving ? <Spinner as="span" animation="border" size="sm" className="me-2" /> : (isEditMode ? t("assignments.edit_button") : 'Kreiraj Vozača')}
                        </Button>
                        <Button variant="outline-secondary" className="w-100 fw-bold font-monospace mt-2" onClick={() => navigate('/drivers')}>
                            Odustani
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default DriverForm;
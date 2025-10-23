// frontend/src/components/AddVehicle.jsx

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createVehicle, fetchDrivers } from '../services/VehicleApi.js';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';
import { FaTruck, FaSave } from 'react-icons/fa';
import { useTranslation } from 'react-i18next'; // ✅ Uvoz za internacionalizaciju


const AddVehicle = () => {
    const { t } = useTranslation(); // ✅ Inicijalizacija
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [drivers, setDrivers] = useState([]);
    const [driversLoading, setDriversLoading] = useState(true);

    const [formData, setFormData] = useState({
        licensePlate: '',
        make: '',
        model: '',
        modelYear: '',
        fuelType: '',
        loadCapacityKg: '',
        currentDriverId: '',
        currentMileageKm: 0,
        nextServiceMileageKm: 0,
        fuelConsumptionLitersPer100Km: 0,
    });

    // Učitavanje liste vozača pri renderu
    useEffect(() => {
        const loadDrivers = async () => {
            setDriversLoading(true);
            try {
                // Dohvaćamo samo potrebne podatke vozača (ID, Ime, Prezime)
                const driverList = await fetchDrivers();
                setDrivers(driverList);
            } catch (err) {
                setError(t('error.fetch_drivers'));
            } finally {
                setDriversLoading(false);
            }
        };

        if (localStorage.getItem('accessToken')) {
            loadDrivers();
        } else {
            // Ako nismo prijavljeni, vozači ne mogu biti učitani (ako je to pravilo Backenda)
            setDriversLoading(false);
        }
    }, [t]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        // Pretvori ID-jeve u Long ili ostavi null ako je prazan string
        const dataToSend = {
            ...formData,
            currentDriverId: formData.currentDriverId ? Number(formData.currentDriverId) : null,
            modelYear: Number(formData.modelYear),
            loadCapacityKg: Number(formData.loadCapacityKg),
            currentMileageKm: Number(formData.currentMileageKm),
            nextServiceMileageKm: Number(formData.nextServiceMileageKm),
            fuelConsumptionLitersPer100Km: Number(formData.fuelConsumptionLitersPer100Km),
        };

        try {
            await createVehicle(dataToSend);
            setSuccess('Vozilo je uspješno kreirano!');
            navigate('/vehicles', { state: { message: 'Vozilo je uspješno kreirano!' } });
        } catch (err) {
            setError(err.message || 'Greška pri kreiranju vozila.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="my-5">
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">
                        {t('forms.create_vehicle_title')}
                    </h2>
                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    {/* Placeholder za loading vozača */}
                    {driversLoading && !error && (
                        <div className="text-center my-3">
                            <Spinner animation="border" variant="info" />
                            <p className="mt-2 font-monospace">{t('general.loading')}</p>
                        </div>
                    )}

                    <Form onSubmit={handleSubmit} className="mt-3">
                        {/* 1. OSNOVNI PODACI */}
                        <div className="row">
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="licensePlate" label={t('forms.license_plate')}>
                                    <Form.Control type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="modelYear" label={t('forms.model_year')}>
                                    <Form.Control type="number" name="modelYear" value={formData.modelYear} onChange={handleChange} required min="1900" max={new Date().getFullYear()} className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        <div className="row">
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="make" label={t('forms.make')}>
                                    <Form.Control type="text" name="make" value={formData.make} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="model" label={t('forms.model')}>
                                    <Form.Control type="text" name="model" value={formData.model} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 2. SPECIFIKACIJE */}
                        <div className="row">
                            <div className="col-md-4 mb-4">
                                <FloatingLabel controlId="fuelType" label={t('forms.fuel_type')}>
                                    <Form.Control type="text" name="fuelType" value={formData.fuelType} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-4 mb-4">
                                <FloatingLabel controlId="loadCapacityKg" label={t('vehicles.load_capacity')}>
                                    <Form.Control type="number" name="loadCapacityKg" value={formData.loadCapacityKg} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-4 mb-4">
                                <FloatingLabel controlId="fuelConsumptionLitersPer100Km" label={t('vehicles.fuel_consumption')}>
                                    <Form.Control type="number" step="0.1" name="fuelConsumptionLitersPer100Km" value={formData.fuelConsumptionLitersPer100Km} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 3. TRENUTNI VOZAČ (Optionalno) */}
                        <FloatingLabel controlId="currentDriverId" label={t('drivers.current_vehicle')} className="mb-4">
                            <Form.Select
                                name="currentDriverId"
                                value={formData.currentDriverId}
                                onChange={handleChange}
                                className="font-monospace"
                                disabled={driversLoading}
                            >
                                <option value="">{t('vehicles.unassigned')}</option>
                                {drivers.map(driver => (
                                    <option key={driver.id} value={driver.id}>
                                        {driver.firstName} {driver.lastName} ({driver.licenseNumber})
                                    </option>
                                ))}
                            </Form.Select>
                        </FloatingLabel>

                        {/* 4. KILOMETRAŽA I SERVIS */}
                        <div className="row">
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="currentMileageKm" label={t('vehicles.current_mileage')}>
                                    <Form.Control
                                        type="number"
                                        name="currentMileageKm"
                                        value={formData.currentMileageKm}
                                        onChange={handleChange}
                                        required
                                        min="0"
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6 mb-4">
                                <FloatingLabel controlId="nextServiceMileageKm" label={t('vehicles.next_service')}>
                                    <Form.Control
                                        type="number"
                                        name="nextServiceMileageKm"
                                        value={formData.nextServiceMileageKm}
                                        onChange={handleChange}
                                        required
                                        // ✅ UKLONJENO OGRANIČENJE min={formData.currentMileageKm}
                                        className="font-monospace"
                                    />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* Gumb za Spremanje */}
                        <Button
                            type="submit"
                            variant="outline-success"
                            className="w-100 fw-bold font-monospace mt-3"
                            disabled={loading}
                        >
                            {loading ? (
                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                            ) : (
                                <><FaSave className='me-2'/> {t('forms.create_vehicle_button')}</>
                            )}
                        </Button>
                        {/* Gumb za Odustajanje */}
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/vehicles')}
                        >
                            {t('general.cancel')}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddVehicle;
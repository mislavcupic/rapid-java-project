// frontend/src/components/AddVehicle.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createVehicle, fetchDrivers } from '../services/VehicleApi.js';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';
import { FaTruck, FaMapPin, FaSave } from 'react-icons/fa'; // Dodana ikona FaSave


const AddVehicle = () => {
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
        currentDriverId: '', // Ostavljamo string '' za selekciju vozača
        currentMileageKm: 0,
        nextServiceMileageKm: 0,
        fuelConsumptionLitersPer100Km: 0,
    });

    // Učitavanje liste vozača pri renderu
    useEffect(() => {
        const loadDrivers = async () => {
            setDriversLoading(true);
            try {
                const driverList = await fetchDrivers();
                setDrivers(driverList);
            } catch (err) {
                setError(`Greška pri učitavanju vozača: ${err.message}. Provjerite ovlasti.`);
                console.error("Greška pri učitavanju vozača:", err);
            } finally {
                setDriversLoading(false);
            }
        };
        loadDrivers();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;

        // KRITIČNA KOREKCIJA LOGIKE:
        // 1. Numerička polja: Pretvaraju se u broj ili se vraća 0 (ako je prazan string)
        // 2. currentDriverId: Ako je prazan string (odabrana '-- Odaberi Vozača...'), ostaje prazan string!
        //    Inače se pretvara u broj (ID vozača).

        let newValue = value;

        if (name === 'currentDriverId') {
            newValue = value === '' ? '' : Number(value); // Ostavlja '' za opcionalno polje
        }
        else if (['modelYear', 'loadCapacityKg', 'currentMileageKm', 'nextServiceMileageKm', 'fuelConsumptionLitersPer100Km'].includes(name)) {
            // Sva numerička polja (osim decimalne potrošnje koju šaljemo kao string u body-ju, ali tretiramo kao Number za input)
            newValue = value ? Number(value) : 0;
        }

        setFormData(prev => ({
            ...prev,
            [name]: newValue
        }));

        setError(null);
        setSuccess(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            // Kreiramo pročišćeni Request DTO za slanje
            const requestData = {
                ...formData,
                // Ako currentDriverId nije postavljen (tj. prazan string ''), šaljemo null API-ju
                currentDriverId: formData.currentDriverId === '' ? null : formData.currentDriverId,

                // Osiguravamo da su numeričke vrijednosti poslane kao pravi tip (iako ih Number() već pretvara, za decimalu je sigurnije)
                loadCapacityKg: Number(formData.loadCapacityKg),
                currentMileageKm: Number(formData.currentMileageKm),
                nextServiceMileageKm: Number(formData.nextServiceMileageKm),
                // Potrošnja je DECIMAL u Javi, šaljemo kao decimalni broj (Float/Number)
                fuelConsumptionLitersPer100Km: Number(formData.fuelConsumptionLitersPer100Km),
            };

            // Logika validacije na Frontendu (npr. sljedeći servis mora biti veći od trenutne km)
            if (requestData.nextServiceMileageKm < requestData.currentMileageKm) {
                throw new Error("Sljedeći servis (km) ne može biti manji od trenutne kilometraže.");
            }


            const newVehicle = await createVehicle(requestData);
            setSuccess(`Vozilo ${newVehicle.licensePlate} (${newVehicle.make} ${newVehicle.model}) uspješno kreirano!`);

            // Preusmjeri na listu
            setTimeout(() => {
                navigate('/vehicles');
            }, 1500);

        } catch (err) {
            setError(`Greška pri kreiranju vozila: ${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    // --- RENDER KOMPONENTE ---

    return (
        <Container className="mt-5 mb-5">
            <Card className="shadow-lg font-monospace">
                <Card.Header className="bg-success text-white">
                    <h4 className='mb-0'><FaTruck className='me-2'/>Dodaj Novo Vozilo</h4>
                </Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    {success && <Alert variant="success">{success}</Alert>}

                    <Form onSubmit={handleSubmit}>

                        {/* 1. RED: Reg. Oznaka i Marka */}
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="licensePlate" label="Registarska Oznaka (ABC-123)">
                                    <Form.Control type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="make" label="Marka Vozila (npr. MAN)">
                                    <Form.Control type="text" name="make" value={formData.make} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 2. RED: Model i Godina */}
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="model" label="Model (npr. TGX)">
                                    <Form.Control type="text" name="model" value={formData.model} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="modelYear" label="Godina Proizvodnje">
                                    <Form.Control type="number" name="modelYear" value={formData.modelYear} onChange={handleChange} required min="1900" max={new Date().getFullYear()} className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 3. RED: Tip Goriva i Nosivost */}
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="fuelType" label="Tip Goriva">
                                    <Form.Control type="text" name="fuelType" value={formData.fuelType} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="loadCapacityKg" label="Nosivost (kg)">
                                    <Form.Control type="number" name="loadCapacityKg" value={formData.loadCapacityKg} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 4. RED: VOZAČ i Potrošnja */}
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="currentDriverId" label="Trenutni Vozač">
                                    <Form.Select
                                        name="currentDriverId"
                                        value={formData.currentDriverId}
                                        onChange={handleChange}
                                        className="font-monospace"
                                        disabled={driversLoading}
                                    >
                                        <option value="">-- Odaberi Vozača (Opcionalno) --</option>
                                        {driversLoading ? (
                                            <option value="" disabled>Učitavanje vozača...</option>
                                        ) : (
                                            // Prikaz dostupnih vozača
                                            drivers.filter(driver => driver && driver.id).map((driver) => (
                                                <option key={driver.id} value={driver.id}>
                                                    {driver.fullName || `Vozač ID: ${driver.id}`}
                                                </option>
                                            ))
                                        )}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="fuelConsumptionLitersPer100Km" label="Potrošnja (L/100km)">
                                    <Form.Control type="number" step="0.1" name="fuelConsumptionLitersPer100Km" value={formData.fuelConsumptionLitersPer100Km} onChange={handleChange} required min="0.1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* 5. RED: Kilometraža i Sljedeći Servis */}
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <FloatingLabel controlId="currentMileageKm" label="Trenutna Kilometraža (km)">
                                    <Form.Control type="number" name="currentMileageKm" value={formData.currentMileageKm} onChange={handleChange} required min="0" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="nextServiceMileageKm" label="Sljedeći Servis (km)">
                                    <Form.Control
                                        type="number"
                                        name="nextServiceMileageKm"
                                        value={formData.nextServiceMileageKm}
                                        onChange={handleChange}
                                        required
                                        // Min vrijednost kontroliramo u validaciji, ali je dobro dodati i ovdje za UI
                                        min={formData.currentMileageKm}
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
                                <><FaSave className='me-2'/> Spremi Vozilo</>
                            )}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddVehicle;
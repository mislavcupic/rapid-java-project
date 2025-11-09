// frontend/src/components/DeliveryConfirmationModal.jsx

import React, { useState } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import { FaSignature, FaCamera, FaMapMarkerAlt } from 'react-icons/fa';

const DeliveryConfirmationModal = ({ show, onHide, onSubmit }) => {
    const [podData, setPodData] = useState({
        recipientName: '',
        recipientSignature: '',
        photoUrl: '',
        notes: '',
        latitude: null,
        longitude: null
    });

    const [gettingLocation, setGettingLocation] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setPodData(prev => ({ ...prev, [name]: value }));
    };

    const handleGetLocation = () => {
        if (!navigator.geolocation) {
            alert('Geolokacija nije podržana u tvom pregledniku.');
            return;
        }

        setGettingLocation(true);
        navigator.geolocation.getCurrentPosition(
            (position) => {
                setPodData(prev => ({
                    ...prev,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                }));
                setGettingLocation(false);
            },
            (error) => {
                alert('Greška pri dohvaćanju lokacije: ' + error.message);
                setGettingLocation(false);
            }
        );
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!podData.recipientName) {
            alert('Molimo unesite ime primatelja!');
            return;
        }

        onSubmit(podData);

        // Reset form
        setPodData({
            recipientName: '',
            recipientSignature: '',
            photoUrl: '',
            notes: '',
            latitude: null,
            longitude: null
        });
    };

    return (
        <Modal show={show} onHide={onHide} size="lg" centered>
            <Modal.Header closeButton className="bg-success text-white">
                <Modal.Title>
                    <FaSignature className="me-2"/> Potvrda Dostave (POD)
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Alert variant="info">
                    Unesi podatke o dostavi kako bi je označio kao završenu.
                </Alert>

                <Form onSubmit={handleSubmit}>
                    {/* Ime primatelja */}
                    <Form.Group className="mb-3">
                        <Form.Label>Ime primatelja *</Form.Label>
                        <Form.Control
                            type="text"
                            name="recipientName"
                            value={podData.recipientName}
                            onChange={handleChange}
                            placeholder="Npr. Ivan Horvat"
                            required
                        />
                    </Form.Group>

                    {/* Potpis (opcijski - u pravom projektu bi koristio canvas) */}
                    <Form.Group className="mb-3">
                        <Form.Label>
                            <FaSignature className="me-1"/> Potpis primatelja (opcijski)
                        </Form.Label>
                        <Form.Control
                            type="text"
                            name="recipientSignature"
                            value={podData.recipientSignature}
                            onChange={handleChange}
                            placeholder="Base64 encoded signature ili text"
                        />
                        <Form.Text className="text-muted">
                            U produkcijskoj verziji ovdje bi bio canvas za crtanje potpisa.
                        </Form.Text>
                    </Form.Group>

                    {/* Fotografija (opcijski) */}
                    <Form.Group className="mb-3">
                        <Form.Label>
                            <FaCamera className="me-1"/> Fotografija dostave (opcijski)
                        </Form.Label>
                        <Form.Control
                            type="text"
                            name="photoUrl"
                            value={podData.photoUrl}
                            onChange={handleChange}
                            placeholder="URL fotografije ili upload"
                        />
                        <Form.Text className="text-muted">
                            U produkcijskoj verziji ovdje bi bio file upload.
                        </Form.Text>
                    </Form.Group>

                    {/* Napomene */}
                    <Form.Group className="mb-3">
                        <Form.Label>Dodatne napomene</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={3}
                            name="notes"
                            value={podData.notes}
                            onChange={handleChange}
                            placeholder="Npr. Ostavljeno na recepciji, kontakt osoba je..."
                        />
                    </Form.Group>

                    {/* GPS Lokacija */}
                    <Form.Group className="mb-3">
                        <Form.Label>
                            <FaMapMarkerAlt className="me-1"/> GPS Lokacija
                        </Form.Label>
                        <div className="d-flex align-items-center">
                            <Button
                                variant="outline-primary"
                                size="sm"
                                onClick={handleGetLocation}
                                disabled={gettingLocation}
                                type="button"
                            >
                                {gettingLocation ? 'Dohvaćam...' : 'Dohvati Trenutnu Lokaciju'}
                            </Button>
                            {podData.latitude && podData.longitude && (
                                <span className="ms-3 text-success fw-bold">
                                    ✓ Lokacija zabilježena
                                </span>
                            )}
                        </div>
                        {podData.latitude && podData.longitude && (
                            <Form.Text className="text-muted d-block mt-2">
                                Lat: {podData.latitude.toFixed(6)}, Lon: {podData.longitude.toFixed(6)}
                            </Form.Text>
                        )}
                    </Form.Group>

                    <Modal.Footer className="border-0 px-0">
                        <Button variant="outline-secondary" onClick={onHide} type="button">
                            Odustani
                        </Button>
                        <Button variant="success" type="submit">
                            Potvrdi Dostavu
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default DeliveryConfirmationModal;
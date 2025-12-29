import React, { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import PropTypes from "prop-types";

const DeliveryConfirmationModal = ({ show, onHide, onSubmit, shipment }) => {
    const [recipientName, setRecipientName] = useState('');
    const [validated, setValidated] = useState(false);

    useEffect(() => {
        if (show) {
            setRecipientName('');
            setValidated(false);
        }
    }, [show]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (recipientName.trim().length < 2) {
            setValidated(true);
            return;
        }
        onSubmit({ recipientName: recipientName.trim() });
    };

    return (
        <Modal show={show} onHide={onHide} centered backdrop="static">
            <Modal.Header closeButton className="bg-success text-white">
                <Modal.Title>Potvrda Isporuke: {shipment?.trackingNumber}</Modal.Title>
            </Modal.Header>
            <Form noValidate validated={validated} onSubmit={handleSubmit}>
                <Modal.Body>
                    <Form.Group>
                        <Form.Label className="fw-bold">Ime i prezime primatelja *</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Tko je preuzeo paket?"
                            required
                            value={recipientName}
                            onChange={(e) => setRecipientName(e.target.value)}
                            isInvalid={validated && recipientName.length < 2}
                        />
                        <Form.Control.Feedback type="invalid">
                            Unos imena je obavezan za završetak isporuke.
                        </Form.Control.Feedback>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onHide}>Odustani</Button>
                    <Button variant="success" type="submit">POTVRDI DOSTAVU</Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default DeliveryConfirmationModal;

DeliveryConfirmationModal.propTypes = {
    show: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    onSubmit: PropTypes.func.isRequired,
    shipment: PropTypes.shape({
        trackingNumber: PropTypes.string
    })
};
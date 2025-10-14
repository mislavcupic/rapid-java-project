import React from 'react';
import { Container, Card, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const AddShipment = () => {
    const navigate = useNavigate();
    return (
        <Container>
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">Kreiranje Nove Pošiljke</h2>
                    <p>Ovo je placeholder forma. Ovdje ide logistika kreiranja pošiljke.</p>
                    <Button variant="secondary" onClick={() => navigate('/shipments')}>
                        Natrag na Pošiljke
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddShipment;
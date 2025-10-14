import React from 'react';
import { Container, Card, Button } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';

const EditAssignment = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    return (
        <Container>
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">Uređivanje Dodjele #{id}</h2>
                    <p>Ovo je placeholder forma. Ovdje ide logistika dodjele vozila i vozača.</p>
                    <Button variant="secondary" onClick={() => navigate('/assignments')}>
                        Natrag na Dodjele
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditAssignment;
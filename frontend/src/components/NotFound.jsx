// frontend/src/components/NotFound.jsx (React Bootstrap Verzija)
import React from 'react';
import { Link } from 'react-router-dom';
import { Card, Button } from 'react-bootstrap';

const NotFound = () => {
    return (
        <div className="d-flex flex-column align-items-center justify-content-center text-center" style={{ minHeight: '70vh' }}>
            <Card className="shadow-lg p-5">
                <Card.Body>
                    <p className="display-1 fw-bolder text-info mb-4 font-monospace">404</p>
                    <h1 className="h3 fw-bold text-dark mb-2 font-monospace">
                        Stranica nije pronađena
                    </h1>
                    <p className="text-muted mb-4">
                        Žao nam je, stranica koju tražite ne postoji.
                    </p>
                    <Button
                        as={Link}
                        to="/"
                        variant="outline-dark" // Outlined gumb
                        className="fw-bold font-monospace"
                    >
                        Povratak na početnu
                    </Button>
                </Card.Body>
            </Card>
        </div>
    );
};

export default NotFound;
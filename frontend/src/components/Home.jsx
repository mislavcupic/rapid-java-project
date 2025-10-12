// frontend/src/components/Home.jsx (React Bootstrap Verzija)
import React from 'react';
import { Link } from 'react-router-dom';
import { Card, Button } from 'react-bootstrap'; // Uvezi Bootstrap komponente

const Home = () => {
    const isAuthenticated = !!localStorage.getItem('accessToken');

    return (
        // Koristimo flexbox klase iz Bootstrapa za centriranje
        <div className="d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '70vh' }}>
            <Card className="text-center shadow-lg border-info border-top-0 border-5 p-4" style={{ maxWidth: '500px' }}>
                <Card.Body>
                    <h1 className="display-5 fw-bold text-dark mb-3">
                        Dobrodošli u Sustav za Logistiku
                    </h1>
                    <p className="lead text-muted mb-4 font-monospace">
                        Upravljajte svojom flotom i logističkim operacijama učinkovito i digitalno.
                    </p>

                    {isAuthenticated ? (
                        <div className="d-grid gap-2">
                            <p className="text-info fw-semibold fs-5">
                                Uspješno ste prijavljeni.
                            </p>
                            <Button
                                as={Link}
                                to="/vehicles"
                                variant="outline-info" // Pastelni outlined gumb
                                size="lg"
                                className="fw-bold font-monospace shadow"
                            >
                                Pregledajte flotu
                            </Button>
                        </div>
                    ) : (
                        <Button
                            as={Link}
                            to="/login"
                            variant="success" // Standardni gumb za prijavu
                            size="lg"
                            className="fw-bold shadow-sm font-monospace"
                        >
                            Prijavite se
                        </Button>
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default Home;
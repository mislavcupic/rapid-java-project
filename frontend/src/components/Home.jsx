// frontend/src/components/Home.jsx (React Bootstrap Verzija)
import React from 'react';
import { Link } from 'react-router-dom';
import { Card, Button } from 'react-bootstrap'; // Uvezi Bootstrap komponente
import { useTranslation } from 'react-i18next';

const Home = () => {
    const { t } = useTranslation();
    const isAuthenticated = !!localStorage.getItem('accessToken');

    return (
        // Koristimo flexbox klase iz Bootstrapa za centriranje
        <div className="d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '70vh' }}>
            <Card className="text-center shadow-lg border-info border-top-0 border-5 p-4" style={{ maxWidth: '500px' }}>
                <Card.Body>
                    <h1 className="display-5 fw-bold text-dark mb-3">
                        {t('messages.welcome_title')}
                    </h1>
                    <p className="lead text-muted mb-4 font-monospace">
                        {t('messages.welcome_text')}
                    </p>

                    {isAuthenticated ? (
                        <div className="d-grid gap-2">
                            <p className="text-info fw-semibold fs-5">
                                {t('messages.login_success')}
                            </p>
                            <Button
                                as={Link}
                                to="/vehicles"
                                variant="outline-info" // Pastelni outlined gumb
                                size="lg"
                                className="fw-bold font-monospace shadow"
                            >
                                {t('messages.view_fleet')}
                            </Button>
                        </div>
                    ) : (
                        <Button
                            as={Link}
                            to="/login"
                            variant="outline-info"
                            size="lg"
                            className="fw-bold shadow-sm font-monospace"
                        >
                            {t('LOGIN')}
                        </Button>
                    )}
                </Card.Body>
            </Card>
        </div>
    );
};

export default Home;

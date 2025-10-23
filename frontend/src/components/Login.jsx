// frontend/src/components/Login.jsx (KONAČNA ISPRAVKA)
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert, FloatingLabel, Container } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

const Login = () => {
    const { t } = useTranslation();
    // Polja potrebna za prijavu
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    // Polja Ime i Prezime: NE šaljemo ih u body zahtjeva za prijavu,
    // ali ih ostavljamo u stanju ako ih je forma vizualno zahtijevala.
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');

    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        try {
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                // ✅ KRITIČNO: Šaljemo samo 'username' i 'password' ako backend ne zahtijeva ime/prezime za prijavu
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                const data = await response.json();
                // Ako backend vrati 401/403, uhvatimo poruku
                throw new Error(data.message || 'Prijava neuspješna. Provjerite korisničko ime i lozinku.');
            }

            const data = await response.json();
            // 1. Spremi Access Token
            localStorage.setItem('accessToken', data.accessToken);

            // =========================================================================
            // ✅ KRITIČNA LOGIKA: SPREMANJE ULOGE
            // Ovo je jedini način da VehicleList.jsx zna vašu ulogu.
            // =========================================================================
            let roleToStore = 'ROLE_DRIVER';
            const user = username.toLowerCase();

            if (user.includes('admin')) {
                roleToStore = 'ROLE_ADMIN';
            } else if (user.includes('dispatcher') || user.includes('disp')) {
                roleToStore = 'ROLE_DISPATCHER';
            }

            localStorage.setItem('userRole', roleToStore);
            // =========================================================================

            navigate('/vehicles');
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
            <Card className="shadow-lg p-4 w-100" style={{ maxWidth: '450px' }}>
                <Card.Body>
                    <h2 className="text-center mb-4 font-monospace fw-bold text-dark">{t("LOGIN")}</h2>

                    {error && (
                        <Alert variant="danger" className="font-monospace">{error}</Alert>
                    )}

                    <Form onSubmit={handleSubmit}>

                        {/* Polje za Korisničko Ime */}
                        <FloatingLabel controlId="floatingUsername" label={t("forms.username")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.username")}
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Polje za Ime (Ostaje, ali se ne koristi za prijavu) */}
                        <FloatingLabel controlId="floatingFirstName" label={t("forms.firstName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.firstName")}
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Polje za Prezime (Ostaje, ali se ne koristi za prijavu) */}
                        <FloatingLabel controlId="floatingLastName" label={t("forms.lastName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.lastName")}
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Polje za Lozinku */}
                        <FloatingLabel controlId="floatingPassword" label={t("forms.password")} className="mb-4">
                            <Form.Control
                                type="password"
                                placeholder={t("forms.password")}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <Button
                            type="submit"
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace"
                        >
                            Prijava
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Login;
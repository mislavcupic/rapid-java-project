// frontend/src/components/Login.jsx (React Bootstrap Verzija S Imenom i Prezimenom)
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert, FloatingLabel, Container } from 'react-bootstrap';

const Login = () => {
    // Dodana polja za Ime i Prezime
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [firstName, setFirstName] = useState(''); // NOVO STANJE
    const [lastName, setLastName] = useState('');   // NOVO STANJE
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        try {
            // U tijelo zahtjeva sada šaljemo i ime i prezime,
            // ako backend to očekuje prilikom prijave ili registracije.
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                // Slanje SVIH podataka
                body: JSON.stringify({ username, password, firstName, lastName }),
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.message || 'Prijava neuspješna');
            }

            const data = await response.json();
            localStorage.setItem('accessToken', data.accessToken);

            navigate('/vehicles');
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        // Koristimo flexbox za centriranje
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
            <Card className="shadow-lg p-4 w-100" style={{ maxWidth: '450px' }}> {/* Povećana širina */}
                <Card.Body>
                    <h2 className="text-center mb-4 font-monospace fw-bold text-dark">Prijava</h2>

                    {error && (
                        <Alert variant="danger" className="font-monospace">{error}</Alert>
                    )}

                    <Form onSubmit={handleSubmit}>

                        {/* Polje za Korisničko Ime */}
                        <FloatingLabel controlId="floatingUsername" label="Korisničko Ime" className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder="Korisničko Ime"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* NOVO POLJE ZA IME */}
                        <FloatingLabel controlId="floatingFirstName" label="Ime" className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder="Ime"
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                required // Postavite prema potrebi
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* NOVO POLJE ZA PREZIME */}
                        <FloatingLabel controlId="floatingLastName" label="Prezime" className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder="Prezime"
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                required // Postavite prema potrebi
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Polje za Lozinku */}
                        <FloatingLabel controlId="floatingPassword" label="Lozinka" className="mb-4">
                            <Form.Control
                                type="password"
                                placeholder="Lozinka"
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
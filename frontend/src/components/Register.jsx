// frontend/src/components/Register.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Card, Alert, FloatingLabel, Container, Spinner } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

const Register = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    
    // Form fields
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    
    // UI state
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        
        // Client-side validation
        if (password !== confirmPassword) {
            setError(t('register.password_mismatch'));
            return;
        }
        
        if (password.length < 6) {
            setError(t('register.password_too_short'));
            return;
        }

        setLoading(true);

        try {
            const response = await fetch('http://localhost:8080/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username,
                    password,
                    firstName,
                    lastName,
                    email
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || t('register.registration_failed'));
            }

            // ✅ Uspješna registracija - spremi token i preusmjeri
            localStorage.setItem('accessToken', data.accessToken);
            
            // Postavi default ulogu (backend vraća ROLE_DRIVER za nove korisnike)
            localStorage.setItem('userRole', 'ROLE_DRIVER');
            
            // Preusmjeri na početnu stranicu
            navigate('/');
            
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
            <Card className="shadow-lg p-4 w-100" style={{ maxWidth: '500px' }}>
                <Card.Body>
                    <h2 className="text-center mb-4 font-monospace fw-bold text-dark">
                        {t('register.title')}
                    </h2>

                    {error && (
                        <Alert variant="danger" className="font-monospace">{error}</Alert>
                    )}

                    <Form onSubmit={handleSubmit}>
                        {/* Username */}
                        <FloatingLabel controlId="floatingUsername" label={t("forms.username")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.username")}
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                minLength={3}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Email */}
                        <FloatingLabel controlId="floatingEmail" label={t("forms.email")} className="mb-3">
                            <Form.Control
                                type="email"
                                placeholder={t("forms.email")}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* First Name */}
                        <FloatingLabel controlId="floatingFirstName" label={t("forms.firstName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.firstName")}
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Last Name */}
                        <FloatingLabel controlId="floatingLastName" label={t("forms.lastName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.lastName")}
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Password */}
                        <FloatingLabel controlId="floatingPassword" label={t("forms.password")} className="mb-3">
                            <Form.Control
                                type="password"
                                placeholder={t("forms.password")}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                minLength={6}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Confirm Password */}
                        <FloatingLabel controlId="floatingConfirmPassword" label={t("register.confirm_password")} className="mb-4">
                            <Form.Control
                                type="password"
                                placeholder={t("register.confirm_password")}
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                minLength={6}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        {/* Submit Button */}
                        <Button
                            type="submit"
                            variant="success"
                            className="w-100 fw-bold font-monospace"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <Spinner
                                        as="span"
                                        animation="border"
                                        size="sm"
                                        role="status"
                                        aria-hidden="true"
                                        className="me-2"
                                    />
                                    {t('register.registering')}
                                </>
                            ) : (
                                t('register.register_button')
                            )}
                        </Button>
                    </Form>

                    {/* Link to Login */}
                    <div className="text-center mt-3">
                        <p className="text-muted font-monospace">
                            {t('register.already_have_account')}{' '}
                            <Link to="/login" className="text-decoration-none fw-bold">
                                {t('LOGIN')}
                            </Link>
                        </p>
                    </div>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default Register;

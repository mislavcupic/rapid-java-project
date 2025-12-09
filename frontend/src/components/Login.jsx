// frontend/src/components/Login.jsx - SA VALIDACIJOM
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert, FloatingLabel, Container } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

const Login = ({ onLoginSuccess }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');

    // Validation errors
    const [errors, setErrors] = useState({});
    const [touched, setTouched] = useState({});
    const [error, setError] = useState(null);

    // Validation functions
    const validateUsername = (value) => {
        if (!value) return t('validation.username_required');
        if (value.length < 3) return t('validation.username_min_length');
        return '';
    };

    const validatePassword = (value) => {
        if (!value) return t('validation.password_required');
        if (value.length < 6) return t('validation.password_min_length');
        return '';
    };

    // Handle blur
    const handleBlur = (field) => {
        setTouched({ ...touched, [field]: true });

        let error = '';
        switch (field) {
            case 'username':
                error = validateUsername(username);
                break;
            case 'password':
                error = validatePassword(password);
                break;
            default:
                break;
        }

        setErrors({ ...errors, [field]: error });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        // Validate all required fields
        const newErrors = {
            username: validateUsername(username),
            password: validatePassword(password)
        };

        setErrors(newErrors);
        setTouched({
            username: true,
            password: true
        });

        // Check if there are any errors
        const hasErrors = Object.values(newErrors).some(err => err !== '');
        if (hasErrors) {
            setError(t('validation.fix_errors'));
            return;
        }

        try {
            localStorage.clear();

            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.message || 'Prijava neuspješna. Provjerite korisničko ime i lozinku.');
            }

            const data = await response.json();

            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('username', username);

            if (data.refreshToken) {
                localStorage.setItem('refreshToken', data.refreshToken);
            }

            try {
                const payload = JSON.parse(atob(data.accessToken.split('.')[1]));
                const authorities = payload.authorities || payload.roles || [];

                if (authorities && authorities.length > 0) {
                    localStorage.setItem('userRoles', JSON.stringify(authorities));

                    if (authorities.includes('ROLE_ADMIN')) {
                        localStorage.setItem('userRole', 'ROLE_ADMIN');
                    } else if (authorities.includes('ROLE_DISPATCHER')) {
                        localStorage.setItem('userRole', 'ROLE_DISPATCHER');
                    } else if (authorities.includes('ROLE_DRIVER')) {
                        localStorage.setItem('userRole', 'ROLE_DRIVER');
                    } else {
                        localStorage.setItem('userRole', authorities[0]);
                    }
                }
            } catch (err) {
                console.error('JWT decode error:', err);
            }

            if (onLoginSuccess) {
                onLoginSuccess();
            }

            navigate('/');
            globalThis.location.reload();

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

                    <Form onSubmit={handleSubmit} noValidate>
                        <FloatingLabel controlId="floatingUsername" label={t("forms.username") + ' *'} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.username")}
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                onBlur={() => handleBlur('username')}
                                isInvalid={touched.username && errors.username}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.username}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        <FloatingLabel controlId="floatingFirstName" label={t("forms.firstName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.firstName")}
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="floatingLastName" label={t("forms.lastName")} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t("forms.lastName")}
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="floatingPassword" label={t("forms.password") + ' *'} className="mb-4">
                            <Form.Control
                                type="password"
                                placeholder={t("forms.password")}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                onBlur={() => handleBlur('password')}
                                isInvalid={touched.password && errors.password}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.password}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        <Button
                            type="submit"
                            variant="outline-primary"
                            className="w-100 fw-bold font-monospace"
                        >
                            {t("LOGIN")}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

Login.propTypes = {
    onLoginSuccess: PropTypes.func
};

Login.defaultProps = {
    onLoginSuccess: null
};

export default Login;
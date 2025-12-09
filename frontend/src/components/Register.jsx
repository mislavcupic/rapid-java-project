// frontend/src/components/Register.jsx - SA VALIDACIJOM I i18n
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
    const [licenseNumber, setLicenseNumber] = useState('');
    const [licenseExpirationDate, setLicenseExpirationDate] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');

    // Validation errors (per field)
    const [errors, setErrors] = useState({});
    const [touched, setTouched] = useState({});

    // UI state
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    // Real-time validation functions
    const validateUsername = (value) => {
        if (!value) return t('validation.username_required');
        if (value.length < 3) return t('validation.username_min_length');
        if (value.length > 50) return t('validation.username_max_length');
        return '';
    };

    const validateEmail = (value) => {
        if (!value) return t('validation.email_required');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) return t('validation.email_invalid');
        return '';
    };

    const validatePassword = (value) => {
        if (!value) return t('validation.password_required');
        if (value.length < 6) return t('validation.password_min_length');
        return '';
    };

    const validateConfirmPassword = (value) => {
        if (!value) return t('validation.confirm_password_required');
        if (value !== password) return t('validation.passwords_dont_match');
        return '';
    };

    const validateFirstName = (value) => {
        if (!value) return t('validation.first_name_required');
        return '';
    };

    const validateLastName = (value) => {
        if (!value) return t('validation.last_name_required');
        return '';
    };

    const validatePhoneNumber = (value) => {
        if (!value) return ''; // Opciono
        const phoneRegex = /^[+]?\d{9,15}$/;
        if (!phoneRegex.test(value)) return t('validation.phone_invalid');
        return '';
    };

    const validateLicenseNumber = (value) => {
        if (!value) return ''; // Opciono
        if (value.length < 5 || value.length > 20) return t('validation.license_number_length');
        return '';
    };

    const validateLicenseExpirationDate = (value) => {
        if (!value) return ''; // Opciono
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate <= today) return t('validation.license_expiration_future');
        return '';
    };

    // Handle blur (field touched)
    const handleBlur = (field) => {
        setTouched({ ...touched, [field]: true });

        let error = '';
        switch (field) {
            case 'username':
                error = validateUsername(username);
                break;
            case 'email':
                error = validateEmail(email);
                break;
            case 'password':
                error = validatePassword(password);
                break;
            case 'confirmPassword':
                error = validateConfirmPassword(confirmPassword);
                break;
            case 'firstName':
                error = validateFirstName(firstName);
                break;
            case 'lastName':
                error = validateLastName(lastName);
                break;
            case 'phoneNumber':
                error = validatePhoneNumber(phoneNumber);
                break;
            case 'licenseNumber':
                error = validateLicenseNumber(licenseNumber);
                break;
            case 'licenseExpirationDate':
                error = validateLicenseExpirationDate(licenseExpirationDate);
                break;
            default:
                break;
        }

        setErrors({ ...errors, [field]: error });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        // Validate all fields
        const newErrors = {
            username: validateUsername(username),
            email: validateEmail(email),
            password: validatePassword(password),
            confirmPassword: validateConfirmPassword(confirmPassword),
            firstName: validateFirstName(firstName),
            lastName: validateLastName(lastName),
            phoneNumber: validatePhoneNumber(phoneNumber),
            licenseNumber: validateLicenseNumber(licenseNumber),
            licenseExpirationDate: validateLicenseExpirationDate(licenseExpirationDate)
        };

        setErrors(newErrors);
        setTouched({
            username: true,
            email: true,
            password: true,
            confirmPassword: true,
            firstName: true,
            lastName: true,
            phoneNumber: true,
            licenseNumber: true,
            licenseExpirationDate: true
        });

        // Check if there are any errors
        const hasErrors = Object.values(newErrors).some(err => err !== '');
        if (hasErrors) {
            setError(t('validation.fix_errors'));
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
                    email,
                    licenseNumber: licenseNumber || null,
                    licenseExpirationDate: licenseExpirationDate || null,
                    phoneNumber: phoneNumber || null
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                // Backend validation errors
                if (data.errors) {
                    setErrors(data.errors);
                    setError(data.message || t('validation.fix_errors'));
                } else {
                    throw new Error(data.message || 'Registracija nije uspjela.');
                }
                return;
            }

            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('userRole', 'ROLE_DRIVER');

            navigate('/');

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center py-5">
            <Card className="shadow-lg p-4 w-100" style={{ maxWidth: '500px' }}>
                <Card.Body>
                    <h2 className="text-center mb-4 font-monospace fw-bold text-dark">
                        {t('forms.registration') || 'Registracija'}
                    </h2>

                    {error && (
                        <Alert variant="danger" className="font-monospace">{error}</Alert>
                    )}

                    <Form onSubmit={handleSubmit} noValidate>
                        {/* Username */}
                        <FloatingLabel controlId="floatingUsername" label={t('forms.username') + ' *'} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t('forms.username')}
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

                        {/* Email */}
                        <FloatingLabel controlId="floatingEmail" label={t('forms.email') + ' *'} className="mb-3">
                            <Form.Control
                                type="email"
                                placeholder={t('forms.email')}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                onBlur={() => handleBlur('email')}
                                isInvalid={touched.email && errors.email}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.email}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        {/* First Name */}
                        <FloatingLabel controlId="floatingFirstName" label={t('forms.firstName') + ' *'} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t('forms.firstName')}
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                onBlur={() => handleBlur('firstName')}
                                isInvalid={touched.firstName && errors.firstName}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.firstName}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        {/* Last Name */}
                        <FloatingLabel controlId="floatingLastName" label={t('forms.lastName') + ' *'} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t('forms.lastName')}
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                onBlur={() => handleBlur('lastName')}
                                isInvalid={touched.lastName && errors.lastName}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.lastName}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        {/* Password */}
                        <FloatingLabel controlId="floatingPassword" label={t('forms.password') + ' *'} className="mb-3">
                            <Form.Control
                                type="password"
                                placeholder={t('forms.password')}
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

                        {/* Confirm Password */}
                        <FloatingLabel controlId="floatingConfirmPassword" label={t('forms.confirm_password') || 'Potvrdi Lozinku' + ' *'} className="mb-4">
                            <Form.Control
                                type="password"
                                placeholder={t('forms.confirm_password') || 'Potvrdi Lozinku'}
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                onBlur={() => handleBlur('confirmPassword')}
                                isInvalid={touched.confirmPassword && errors.confirmPassword}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.confirmPassword}
                            </Form.Control.Feedback>
                        </FloatingLabel>

                        {/* Divider */}
                        <hr className="my-4" />
                        <h5 className="text-center mb-3 text-muted font-monospace">
                            {t('forms.driver_info_optional') || 'Driver Podaci (Opciono)'}
                        </h5>

                        {/* License Number */}
                        <FloatingLabel controlId="floatingLicenseNumber" label={t('forms.license_number') || 'Broj Vozačke Dozvole'} className="mb-3">
                            <Form.Control
                                type="text"
                                placeholder={t('forms.license_number')}
                                value={licenseNumber}
                                onChange={(e) => setLicenseNumber(e.target.value)}
                                onBlur={() => handleBlur('licenseNumber')}
                                isInvalid={touched.licenseNumber && errors.licenseNumber}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.licenseNumber}
                            </Form.Control.Feedback>
                            <Form.Text className="text-muted">
                                {t('forms.optional_later') || 'Opciono - možeš unijeti kasnije'} (5-20 {t('forms.characters') || 'znakova'})
                            </Form.Text>
                        </FloatingLabel>

                        {/* License Expiration Date */}
                        <FloatingLabel controlId="floatingLicenseExpiration" label={t('forms.license_expiration') || 'Datum Isteka Dozvole'} className="mb-3">
                            <Form.Control
                                type="date"
                                placeholder={t('forms.license_expiration')}
                                value={licenseExpirationDate}
                                onChange={(e) => setLicenseExpirationDate(e.target.value)}
                                onBlur={() => handleBlur('licenseExpirationDate')}
                                isInvalid={touched.licenseExpirationDate && errors.licenseExpirationDate}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.licenseExpirationDate}
                            </Form.Control.Feedback>
                            <Form.Text className="text-muted">
                                {t('forms.must_be_future') || 'Opciono - mora biti u budućnosti'}
                            </Form.Text>
                        </FloatingLabel>

                        {/* Phone Number */}
                        <FloatingLabel controlId="floatingPhoneNumber" label={t('forms.phone') || 'Telefonski Broj'} className="mb-4">
                            <Form.Control
                                type="tel"
                                placeholder={t('forms.phone')}
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                                onBlur={() => handleBlur('phoneNumber')}
                                isInvalid={touched.phoneNumber && errors.phoneNumber}
                                className="font-monospace"
                            />
                            <Form.Control.Feedback type="invalid">
                                {errors.phoneNumber}
                            </Form.Control.Feedback>
                            <Form.Text className="text-muted">
                                {t('forms.phone_format') || 'Format: +385912345678 (9-15 brojeva)'}
                            </Form.Text>
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
                                        aria-hidden="true"
                                        className="me-2"
                                    />
                                    {t('forms.registering') || 'Registriram...'}
                                </>
                            ) : (
                                t('forms.register_button') || 'Registriraj se'
                            )}
                        </Button>
                    </Form>

                    {/* Link to Login */}
                    <div className="text-center mt-3">
                        <p className="text-muted font-monospace">
                            {t('forms.already_have_account') || 'Već imaš račun?'}{' '}
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
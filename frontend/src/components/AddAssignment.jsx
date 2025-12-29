import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Alert } from 'react-bootstrap';
import { createAssignment } from '../services/AssignmentApi';
import { useTranslation } from 'react-i18next';
import AssignmentForm from '../components/AssignmentForm';

const AddAssignment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleCreate = async (finalData) => {
        setLoading(true);
        setError(null);
        try {
            // finalData dolazi već formatiran (ID-ovi kao brojevi) iz AssignmentForm-a
            await createAssignment(finalData);
            navigate('/assignments');
        } catch (err) {
            setError(err.message || "Greška pri spremanju");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-4">
            <Card className="shadow-sm border-0">
                <Card.Header className="bg-primary text-white py-3">
                    <h4 className="mb-0 font-monospace">{t('assignments.new_assignment')}</h4>
                </Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <AssignmentForm
                        onSubmit={handleCreate}
                        saving={loading}
                    />
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddAssignment;
import React, { useState, useEffect } from 'react';
import { Container, Card, Alert, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { fetchAssignmentById, updateAssignment } from '../services/AssignmentApi';
import AssignmentForm from '../components/AssignmentForm';

const EditAssignment = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();

    const [assignment, setAssignment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = async () => {
            try {
                const data = await fetchAssignmentById(id);
                // Pretvaramo podatke iz baze u format koji forma razumije
                const formattedData = {
                    driverId: data.driver?.id,
                    vehicleId: data.vehicle?.id,
                    shipmentIds: data.shipments?.map(s => String(s.id)),
                    startTime: data.startTime
                };
                setAssignment(formattedData);
            } catch (err) {
                setError(t('messages.error_loading_assignment, {}',err));
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [id, t]);

    const handleUpdate = async (formData) => {
        setSaving(true);
        try {
            await updateAssignment(id, formData);
            navigate('/assignments', { state: { message: t('messages.assignment_updated_success') } });
        } catch (err) {
            setError(err.message || "Greška pri ažuriranju");
            setSaving(false);
        }
    };

    if (loading) return <div className="text-center p-5"><Spinner animation="border" /></div>;

    return (
        <Container className="py-4">
            <Card className="shadow-sm border-0">
                <Card.Header className="bg-warning py-3">
                    <h4 className="mb-0">{t('general.edit_assignment')} #{id}</h4>
                </Card.Header>
                <Card.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <AssignmentForm
                        initialData={assignment}
                        onSubmit={handleUpdate}
                        saving={saving}
                    />
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditAssignment;
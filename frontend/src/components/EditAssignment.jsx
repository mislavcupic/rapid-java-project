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
            setLoading(true); // Resetiramo loading pri promjeni ID-a
            try {
                const data = await fetchAssignmentById(id);

                // ✅ Formatiranje podataka za AssignmentForm
                const formattedData = {
                    driverId: data.driver?.id ? String(data.driver.id) : '',
                    vehicleId: data.vehicle?.id ? String(data.vehicle.id) : '',
                    shipmentIds: data.shipments?.map(s => String(s.id)) || [],
                    // Osiguravamo da datum odgovara HTML input formatu (yyyy-MM-ddThh:mm)
                    startTime: data.startTime ? data.startTime.slice(0, 16) : '',
                    status: data.status || ''
                };

                setAssignment(formattedData);
                setError(null);
            } catch (err) {
                console.error("Greška pri učitavanju:", err);
                setError(t('messages.error_loading_assignment'));
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [id, t]);

    const handleUpdate = async (formData) => {
        // ✅ Sprječavanje višestrukih slanja dok je saving: true
        if (saving) return;

        setSaving(true);
        setError(null); // Čistimo prethodne greške pri novom pokušaju

        try {
            await updateAssignment(id, formData);
            // Navigacija s porukom o uspjehu
            navigate('/assignments', {
                state: { message: t('messages.assignment_updated_success') },
                replace: true
            });
        } catch (err) {
            console.error("Greška pri spremanju:", err);
            // Provjera ima li server specifičnu poruku greške
            const errorMessage = err.response?.data?.message || err.message || "Greška pri ažuriranju";
            setError(errorMessage);
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="text-center p-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">{t('general.loading')}</p>
            </div>
        );
    }

    return (
        <Container className="py-4">
            <Card className="shadow-sm border-0">
                <Card.Header className="bg-warning py-3 text-dark d-flex justify-content-between align-items-center">
                    <h4 className="mb-0 fw-bold">
                        {t('general.edit_assignment')} #{id}
                    </h4>
                    <span className="badge bg-dark">ID: {id}</span>
                </Card.Header>
                <Card.Body className="p-4">
                    {error && (
                        <Alert variant="danger" onClose={() => setError(null)} dismissible>
                            <i className="bi bi-exclamation-triangle-fill me-2"></i>
                            {error}
                        </Alert>
                    )}

                    {/* ✅ AssignmentForm mora primiti onSubmit prop točno pod tim imenom */}
                    {assignment && (
                        <AssignmentForm
                            initialData={assignment}
                            onSubmit={handleUpdate}
                            saving={saving}
                        />
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditAssignment;
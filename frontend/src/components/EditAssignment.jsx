import React from 'react';
import { Container, Card, Button } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const EditAssignment = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    return (
        <Container>
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">
                        {t('general.edit')} {t('Assignments')} #{id}
                    </h2>
                    <p>{t("forms.placeholder_text")}</p>
                    <Button variant="secondary" onClick={() => navigate('/assignments')}>
                        Natrag na {t('Assignments')}
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditAssignment;
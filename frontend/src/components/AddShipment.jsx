import React from 'react';
import { Container, Card, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const AddShipment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    return (
        <Container>
            <Card className="shadow-lg p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">{t("forms.create_shipment_title")}</h2>
                    <p>{t("forms.placeholder_text")}</p>
                    <Button variant="secondary" onClick={() => navigate('/shipments')}>
                        Natrag na {t("Shipments")}
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddShipment;
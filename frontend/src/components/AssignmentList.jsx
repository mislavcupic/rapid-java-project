import React, { useState, useEffect, useCallback } from 'react';
import { fetchAssignments, deleteAssignment } from '../services/AssignmentApi';
import { Table, Alert, Button, Card, Spinner, Modal } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { FaEdit, FaTrash, FaPlus } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const AssignmentList = () => {
    const { t } = useTranslation();
    const [assignments, setAssignments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [assignmentToDelete, setAssignmentToDelete] = useState(null);
    const navigate = useNavigate();

    const loadAssignments = useCallback(async () => {
        try {
            setLoading(true);
            const data = await fetchAssignments();
            setAssignments(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadAssignments();
    }, [loadAssignments]);

    const confirmDelete = async () => {
        try {
            await deleteAssignment(assignmentToDelete.id);
            setShowDeleteModal(false);
            loadAssignments();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <Card className="shadow-sm border-0">
            <Card.Header className="bg-white py-3 d-flex justify-content-between align-items-center">
                <h4 className="mb-0 font-monospace text-primary">{t("assignments.title")}</h4>
                <Button variant="primary" onClick={() => navigate('/assignments/new')}>
                    <FaPlus className="me-2" /> {t("assignments.new_assignment")}
                </Button>
            </Card.Header>
            <Card.Body>
                {loading ? <Spinner animation="border" /> : (
                    <Table responsive hover className="align-middle">
                        <thead className="table-light">
                        <tr>
                            <th>ID</th>
                            <th>{t("assignments.driver")}</th>
                            <th>{t("assignments.vehicle")}</th>
                            <th>{t("assignments.shipments")}</th>
                            <th>{t("assignments.status")}</th>
                            <th>{t("general.actions")}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {assignments.map(a => (
                            <tr key={a.id}>
                                <td className="fw-bold">#{a.id}</td>
                                <td>{a.driver?.firstName} {a.driver?.lastName}</td>
                                <td>{a.vehicle?.licensePlate}</td>
                                <td>
                                    {a.shipments?.map(s => (
                                        <div key={s.id} className="small text-muted">📦 {s.trackingNumber}</div>
                                    ))}
                                </td>
                                <td><span className="badge bg-info">{a.assignmentStatus}</span></td>
                                <td>
                                    <Button variant="link" onClick={() => navigate(`/assignments/edit/${a.id}`)}><FaEdit /></Button>
                                    <Button variant="link" className="text-danger" onClick={() => { setAssignmentToDelete(a); setShowDeleteModal(true); }}><FaTrash /></Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                )}
            </Card.Body>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
                <Modal.Header closeButton><Modal.Title>{t("messages.confirm_delete_title")}</Modal.Title></Modal.Header>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>{t("general.cancel")}</Button>
                    <Button variant="danger" onClick={confirmDelete}>{t("general.delete")}</Button>
                </Modal.Footer>
            </Modal>
        </Card>
    );
};

export default AssignmentList;
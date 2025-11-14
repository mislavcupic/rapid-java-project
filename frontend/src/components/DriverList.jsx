// frontend/src/components/DriverList.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { fetchDrivers, deleteDriver, updateDriver } from '../services/DriverApi';
import { Table, Alert, Button, Card, Spinner, Modal, Container, Form, FloatingLabel } from 'react-bootstrap';
import { useNavigate, Link } from 'react-router-dom';
import { FaEdit, FaTrash, FaPlus, FaEye } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const DriverList = () => {
    const { t } = useTranslation();
    const [drivers, setDrivers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAuthenticated] = useState(!!localStorage.getItem('accessToken'));

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [driverToDelete, setDriverToDelete] = useState(null);

    const [showEditModal, setShowEditModal] = useState(false);
    const [driverToEdit, setDriverToEdit] = useState(null);
    const [editFormData, setEditFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        licenseNumber: '',
        licenseExpirationDate: '',
        phoneNumber: ''
    });

    const [showViewModal, setShowViewModal] = useState(false);
    const [driverToView, setDriverToView] = useState(null);

    const [deleteSuccess, setDeleteSuccess] = useState(null);
    const [editSuccess, setEditSuccess] = useState(null);
    const [saving, setSaving] = useState(false);

    //const navigate = useNavigate();

    const userRole = localStorage.getItem('userRole');
    const isAdmin = userRole?.includes('ROLE_ADMIN');
    const isDispatcherOrAdmin = isAdmin || (userRole?.includes('ROLE_DISPATCHER'));

    const loadDrivers = useCallback(async () => {
        if (!isAuthenticated) {
            setError(t('messages.user_not_logged_in'));
            setLoading(false);
            return;
        }
        try {
            const data = await fetchDrivers();
            setDrivers(data);
            setError(null);
        } catch (err) {
            console.error("Greška pri učitavanju vozača:", err);
            setError(err.message || t('messages.error_fetching_drivers'));
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated, t]);

    useEffect(() => {
        loadDrivers();
    }, [loadDrivers]);

    const handleViewClick = (driver) => {
        setDriverToView(driver);
        setShowViewModal(true);
    };

    const handleEditClick = (driver) => {
        if (!isDispatcherOrAdmin) {
            setError(t('messages.access_denied_edit_drivers'));
            return;
        }
        setError(null);
        setDriverToEdit(driver);
        setEditFormData({
            firstName: driver.firstName || '',
            lastName: driver.lastName || '',
            email: driver.email || '',
            licenseNumber: driver.licenseNumber || '',
            licenseExpirationDate: driver.licenseExpirationDate || '',
            phoneNumber: driver.phoneNumber || ''
        });
        setShowEditModal(true);
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditFormData(prev => ({ ...prev, [name]: value }));
    };

    const confirmEdit = async () => {
        if (!driverToEdit) return;

        setSaving(true);
        setError(null);

        try {
            const updatedDriver = await updateDriver(driverToEdit.id, editFormData);

            setEditSuccess(t('messages.driver_updated_success', {
                name: `${updatedDriver.firstName} ${updatedDriver.lastName}`
            }));

            setDrivers(prevDrivers =>
                prevDrivers.map(d => d.id === driverToEdit.id ? updatedDriver : d)
            );

            setShowEditModal(false);
            setDriverToEdit(null);

            setTimeout(() => setEditSuccess(null), 3000);

        } catch (err) {
            setError(err.message || t('messages.update_failed'));
        } finally {
            setSaving(false);
        }
    };

    const handleDeleteClick = (driver) => {
        if (!isAdmin) {
            setError(t('messages.access_denied_delete_drivers'));
            return;
        }
        setError(null);
        setDriverToDelete(driver);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        setShowDeleteModal(false);
        if (!driverToDelete) return;

        try {
            await deleteDriver(driverToDelete.id);
            setDeleteSuccess(t('messages.driver_deleted_success', {
                name: `${driverToDelete.firstName} ${driverToDelete.lastName}`
            }));
            setDrivers(prevDrivers => prevDrivers.filter(d => d.id !== driverToDelete.id));
            setDriverToDelete(null);
            setTimeout(() => setDeleteSuccess(null), 3000);
        } catch (err) {
            setError(err.message || t('messages.delete_failed'));
        }
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="success"  />
                <p className="text-muted mt-2">{t('messages.loading_drivers')}</p>
            </div>
        );
    }

    if (error && !deleteSuccess && !editSuccess) {
        return <Alert variant="danger" className="font-monospace">{error}</Alert>;
    }

    if (!isAuthenticated) {
        return <Alert variant="warning" className="font-monospace">{t('messages.access_denied')}</Alert>;
    }

    return (
        <Container fluid>
            <Card className="shadow-lg border-success border-top-0 border-5">
                <Card.Header className="bg-success text-white d-flex justify-content-between align-items-center">
                    <h1 className="h4 mb-0 font-monospace">{t('drivers.driver_list')}</h1>
                    <Button
                        as={Link}
                        to="/drivers/add"
                        variant="outline-light"
                        className="fw-bold font-monospace"
                        size="sm"
                        disabled={!isDispatcherOrAdmin}
                        title={!isDispatcherOrAdmin ? t('messages.access_denied_add_drivers') : t('drivers.add_driver')}
                    >
                        <FaPlus className="me-2" />
                        {t('drivers.add_driver')}
                    </Button>
                </Card.Header>
                <Card.Body>
                    {deleteSuccess && <Alert variant="success" className="font-monospace">{deleteSuccess}</Alert>}
                    {editSuccess && <Alert variant="success" className="font-monospace">{editSuccess}</Alert>}

                    {drivers.length === 0 ? (
                        <Alert variant="info" className="font-monospace">
                            {t('messages.no_drivers')}
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table striped bordered hover className="text-center align-middle font-monospace">
                                <thead className="table-light">
                                <tr>
                                    <th>ID</th>
                                    <th>{t('forms.driver_name')}</th>
                                    <th>{t('forms.email')}</th>
                                    <th>{t('forms.license')}</th>
                                    <th>{t('forms.phone')}</th>
                                    <th>{t('general.actions')}</th>
                                </tr>
                                </thead>
                                <tbody>
                                {drivers.map((driver) => (
                                    <tr key={driver.id}>
                                        <td>{driver.id}</td>
                                        <td>{driver.firstName} {driver.lastName}</td>
                                        <td>{driver.email}</td>
                                        <td>{driver.licenseNumber}</td>
                                        <td>{driver.phoneNumber || 'N/A'}</td>
                                        <td className="text-nowrap">
                                            <div className="d-grid gap-2 d-md-flex justify-content-md-center">
                                                <Button
                                                    variant="outline-info"
                                                    size="sm"
                                                    onClick={() => handleViewClick(driver)}
                                                    title={t('general.view')}
                                                >
                                                    <FaEye />
                                                </Button>

                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    onClick={() => handleEditClick(driver)}
                                                    title={!isDispatcherOrAdmin ? t('messages.access_denied_edit_drivers') : t('general.edit')}
                                                    disabled={!isDispatcherOrAdmin}
                                                >
                                                    <FaEdit />
                                                </Button>

                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => handleDeleteClick(driver)}
                                                    title={!isAdmin ? t('messages.access_denied_delete_drivers') : t('general.delete')}
                                                    disabled={!isAdmin}
                                                >
                                                    <FaTrash />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            {/* VIEW MODAL */}
            <Modal show={showViewModal} onHide={() => setShowViewModal(false)} centered>
                <Modal.Header closeButton className="bg-info text-white">
                    <Modal.Title className="font-monospace">
                        <FaEye className="me-2" />
                        {t('drivers.driver_details')}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    {driverToView && (
                        <div>
                            <p><strong>ID:</strong> {driverToView.id}</p>
                            <p><strong>{t('forms.first_name')}:</strong> {driverToView.firstName}</p>
                            <p><strong>{t('forms.last_name')}:</strong> {driverToView.lastName}</p>
                            <p><strong>{t('forms.email')}:</strong> {driverToView.email}</p>
                            <p><strong>{t('forms.username')}:</strong> {driverToView.username}</p>
                            <p><strong>{t('forms.license')}:</strong> {driverToView.licenseNumber}</p>
                            <p><strong>{t('forms.license_expiration')}:</strong> {driverToView.licenseExpirationDate || 'N/A'}</p>
                            <p><strong>{t('forms.phone')}:</strong> {driverToView.phoneNumber || 'N/A'}</p>
                        </div>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowViewModal(false)} className="font-monospace">
                        {t('general.close')}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* EDIT MODAL */}
            <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered>
                <Modal.Header closeButton className="bg-primary text-white">
                    <Modal.Title className="font-monospace">
                        <FaEdit className="me-2" />
                        {t('drivers.edit_driver')}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    {error && <Alert variant="danger">{error}</Alert>}

                    <Form>
                        <FloatingLabel controlId="firstName" label={t('forms.first_name')} className="mb-3">
                            <Form.Control
                                type="text"
                                name="firstName"
                                value={editFormData.firstName}
                                onChange={handleEditChange}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="lastName" label={t('forms.last_name')} className="mb-3">
                            <Form.Control
                                type="text"
                                name="lastName"
                                value={editFormData.lastName}
                                onChange={handleEditChange}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="email" label={t('forms.email')} className="mb-3">
                            <Form.Control
                                type="email"
                                name="email"
                                value={editFormData.email}
                                onChange={handleEditChange}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="licenseNumber" label={t('forms.license')} className="mb-3">
                            <Form.Control
                                type="text"
                                name="licenseNumber"
                                value={editFormData.licenseNumber}
                                onChange={handleEditChange}
                                required
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="licenseExpirationDate" label={t('forms.license_expiration')} className="mb-3">
                            <Form.Control
                                type="date"
                                name="licenseExpirationDate"
                                value={editFormData.licenseExpirationDate}
                                onChange={handleEditChange}
                                className="font-monospace"
                            />
                        </FloatingLabel>

                        <FloatingLabel controlId="phoneNumber" label={t('forms.phone')} className="mb-3">
                            <Form.Control
                                type="text"
                                name="phoneNumber"
                                value={editFormData.phoneNumber}
                                onChange={handleEditChange}
                                className="font-monospace"
                            />
                        </FloatingLabel>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        variant="outline-secondary"
                        onClick={() => setShowEditModal(false)}
                        className="font-monospace"
                        disabled={saving}
                    >
                        {t('general.cancel')}
                    </Button>
                    <Button
                        variant="success"
                        onClick={confirmEdit}
                        className="font-monospace"
                        disabled={saving}
                    >
                        {saving ? (
                            <>
                                <Spinner as="span" animation="border" size="sm" className="me-2" />
                                {t('general.saving')}
                            </>
                        ) : (
                            t('general.save')
                        )}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* DELETE MODAL */}
            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="font-monospace text-danger">
                        {t('messages.confirm_delete_title')}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body className="font-monospace">
                    {t('messages.confirm_delete_driver', {
                        name: `${driverToDelete?.firstName} ${driverToDelete?.lastName}`
                    })}
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        variant="outline-secondary"
                        onClick={() => setShowDeleteModal(false)}
                        className="font-monospace"
                    >
                        {t('general.cancel')}
                    </Button>
                    <Button
                        variant="danger"
                        onClick={confirmDelete}
                        className="font-monospace"
                    >
                        {t('general.delete_permanent')}
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default DriverList;
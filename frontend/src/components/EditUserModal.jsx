import { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import PropTypes from 'prop-types';

function EditUserModal({ show, user, onClose, onSave }) {
    const availableRoles = [
        { name: 'ROLE_ADMIN', label: 'Administrator' },
        { name: 'ROLE_DISPATCHER', label: 'Dispatcher' },
        { name: 'ROLE_DRIVER', label: 'Vozač' }
    ];

    const [selectedRoles, setSelectedRoles] = useState([]);

    useEffect(() => {
        if (user?.roles) {
            const roleNames = user.roles.map((role) => role.name);
            setSelectedRoles(roleNames);
        }
    }, [user]);

    const handleRoleChange = (roleName) => {
        setSelectedRoles((prevRoles) => {
            if (prevRoles.includes(roleName)) {
                return prevRoles.filter((r) => r !== roleName);
            }
            return [...prevRoles, roleName];
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (selectedRoles.length === 0) {
            alert('Morate odabrati barem jednu ulogu!');
            return;
        }
        onSave(user.id, selectedRoles);
    };

    if (!user) {
        return null;
    }

    return (
        <Modal show={show} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Uredi uloge korisnika</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <div className="mb-3">
                    <strong>Korisnik:</strong> {user.username}
                    <br />
                    <strong>Ime:</strong> {`${user.firstName} ${user.lastName}`}
                </div>

                <Form onSubmit={handleSubmit}>
                    <Form.Group>
                        <Form.Label className="fw-bold">Odaberi uloge:</Form.Label>
                        {availableRoles.map((role) => (
                            <Form.Check
                                key={role.name}
                                type="checkbox"
                                id={`role-${role.name}`}
                                label={role.label}
                                checked={selectedRoles.includes(role.name)}
                                onChange={() => handleRoleChange(role.name)}
                            />
                        ))}
                    </Form.Group>

                    <div className="d-flex justify-content-end gap-2 mt-4">
                        <Button variant="secondary" onClick={onClose}>
                            Odustani
                        </Button>
                        <Button variant="primary" type="submit">
                            Spremi
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
}

EditUserModal.propTypes = {
    show: PropTypes.bool.isRequired,
    user: PropTypes.shape({
        id: PropTypes.number.isRequired,
        username: PropTypes.string.isRequired,
        firstName: PropTypes.string.isRequired,
        lastName: PropTypes.string.isRequired,
        roles: PropTypes.arrayOf(
            PropTypes.shape({
                id: PropTypes.number.isRequired,
                name: PropTypes.string.isRequired
            })
        )
    }),
    onClose: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired
};

EditUserModal.defaultProps = {
    user: null
};

export default EditUserModal;
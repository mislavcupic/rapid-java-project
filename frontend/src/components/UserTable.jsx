import { Table, Button, Badge } from 'react-bootstrap';
import PropTypes from 'prop-types';

function UserTable({ users, onEditUser, onDeleteUser }) {
    const getRoleBadgeVariant = (roleName) => {
        const roleMap = {
            'ROLE_ADMIN': 'danger',
            'ROLE_DISPATCHER': 'warning',
            'ROLE_DRIVER': 'primary',
            'ROLE_USER': 'secondary'
        };
        return roleMap[roleName] || 'secondary';
    };

    return (
        <Table striped bordered hover responsive>
            <thead>
            <tr>
                <th>ID</th>
                <th>Korisničko ime</th>
                <th>Ime i prezime</th>
                <th>Email</th>
                <th>Uloge</th>
                <th>Status</th>
                <th>Akcije</th>
            </tr>
            </thead>
            <tbody>
            {users.length === 0 ? (
                <tr>
                    <td colSpan="7" className="text-center">
                        Nema korisnika za prikaz
                    </td>
                </tr>
            ) : (
                users.map((user) => (
                    <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.username}</td>
                        <td>{`${user.firstName} ${user.lastName}`}</td>
                        <td>{user.email}</td>
                        <td>
                            {user?.roles.map((role) => (
                                <Badge
                                    key={role.id}
                                    bg={getRoleBadgeVariant(role.name)}
                                    className="me-1"
                                >
                                    {role.name.replace('ROLE_', '')}
                                </Badge>
                            ))}
                        </td>
                        <td>
                            <Badge bg={user.isEnabled ? 'success' : 'secondary'}>
                                {user.isEnabled ? 'Aktivan' : 'Neaktivan'}
                            </Badge>
                        </td>
                        <td>
                            <Button
                                variant="outline-primary"
                                size="sm"
                                className="me-2"
                                onClick={() => onEditUser(user)}
                            >
                                Uredi uloge
                            </Button>
                            <Button
                                variant="outline-danger"
                                size="sm"
                                onClick={() => onDeleteUser(user.id)}
                            >
                                Obriši
                            </Button>
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </Table>
    );
}

UserTable.propTypes = {
    users: PropTypes.arrayOf(
        PropTypes.shape({
            id: PropTypes.number.isRequired,
            username: PropTypes.string.isRequired,
            firstName: PropTypes.string.isRequired,
            lastName: PropTypes.string.isRequired,
            email: PropTypes.string.isRequired,
            isEnabled: PropTypes.bool.isRequired,
            roles: PropTypes.arrayOf(
                PropTypes.shape({
                    id: PropTypes.number.isRequired,
                    name: PropTypes.string.isRequired
                })
            )
        })
    ).isRequired,
    onEditUser: PropTypes.func.isRequired,
    onDeleteUser: PropTypes.func.isRequired
};

export default UserTable;
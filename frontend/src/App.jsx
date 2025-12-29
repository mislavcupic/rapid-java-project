import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button, NavDropdown } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';
import { jwtDecode } from 'jwt-decode';

// Import svih ruta/komponenti
import AddVehicle from './components/AddVehicle.jsx';
import VehicleList from './components/VehicleList.jsx';
import Login from './components/Login.jsx';
import Home from './components/Home.jsx';
import NotFound from './components/NotFound.jsx';
import EditVehicle from './components/EditVehicle.jsx';
import AssignmentList from './components/AssignmentList.jsx';
import ShipmentList from './components/ShipmentList.jsx';
import AddShipment from './components/AddShipment.jsx';
import EditAssignment from './components/EditAssignment.jsx';
import EditShipment from './components/EditShipment.jsx';
import AddAssignment from "./components/AddAssignment.jsx";
import DriverForm from "./components/DriverForm.jsx";
import DriverList from "./components/DriverList.jsx";
import AnalyticsPage from "./components/AnalyticsPage.jsx";
import Register from "./components/Register.jsx";
import ShipmentDetails from "./components/ShipmentDetails.jsx";
import DriverAssignmentDetails from "./components/DriverAssignmentDetails.jsx";
import DriverDashboard from "./components/DriverDashboard.jsx";
import AdminDashboard from "./components/AdminDashboard.jsx";
import PropTypes from 'prop-types';
import AssignmentDetails from "./components/AssignmentsDetails.jsx";


const AppNavbar = ({ userRoles, onLogout }) => {
    const { t, i18n } = useTranslation();
    const isAuthenticated = !!localStorage.getItem('accessToken');

    // ✅ Provjera pojedinačnih uloga
    const isAdmin = userRoles.includes('ROLE_ADMIN');
    const isDispatcher = userRoles.includes('ROLE_DISPATCHER');
    const isDriver = userRoles.includes('ROLE_DRIVER');

    const changeLanguage = (lng) => {
        i18n.changeLanguage(lng);
    };

    return (
        <Navbar bg="dark" variant="dark" expand="lg" sticky="top" className="shadow-lg">
            <Container fluid className="px-md-5">
                <Navbar.Brand as={Link} to="/" className="font-monospace text-info fs-4 fw-bold">
                    FLEET.IO
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">

                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/">{t('Home')}</Nav.Link>
                        {isAuthenticated && (
                            <>
                                {/* ✅ Vozila, Vozači, Pošiljke, Assignments - vidljivo za sve autentificirane */}
                                <Nav.Link as={Link} to="/vehicles">{t('Vehicles')}</Nav.Link>
                                <Nav.Link as={Link} to="/drivers">{t('Drivers')}</Nav.Link>
                                <Nav.Link as={Link} to="/shipments">{t('Shipments')}</Nav.Link>
                                <Nav.Link as={Link} to="/assignments">{t('Assignments')}</Nav.Link>

                                {/* ✅ Dashboards - Dropdown ako ima više uloga */}
                                {(isAdmin || isDispatcher || isDriver) && (
                                    <NavDropdown title={t('Dashboards')} id="dashboards-dropdown">
                                        {isAdmin && (
                                            <NavDropdown.Item as={Link} to="/admin/users">
                                                {t('Admin Dashboard')}
                                            </NavDropdown.Item>
                                        )}
                                        {isDispatcher && (
                                            <NavDropdown.Item as={Link} to="/dispatcher/dashboard">
                                                {t('Dispatcher Dashboard')}
                                            </NavDropdown.Item>
                                        )}
                                        {isDriver && (
                                            <NavDropdown.Item as={Link} to="/driver/dashboard">
                                                {t('Driver Dashboard')}
                                            </NavDropdown.Item>
                                        )}
                                    </NavDropdown>
                                )}

                                {/* ✅ Analytics - samo Admin i Dispatcher */}
                                {(isAdmin || isDispatcher) && (
                                    <Nav.Link as={Link} to="/analytics">{t('Analytics')}</Nav.Link>
                                )}
                            </>
                        )}
                    </Nav>

                    <Nav className="align-items-center">
                        <div className="d-flex me-3">
                            {['en', 'fr', 'hr'].map(lng => (
                                <Button
                                    key={lng}
                                    variant={i18n.language === lng ? 'info' : 'outline-info'}
                                    size="sm"
                                    onClick={() => changeLanguage(lng)}
                                    className="me-1"
                                >
                                    {lng.toUpperCase()}
                                </Button>
                            ))}
                        </div>

                        {isAuthenticated ? (
                            <Button
                                onClick={onLogout}
                                variant="outline-danger"
                                className="font-monospace"
                            >
                                {t('LOGOUT')}
                            </Button>
                        ) : (
                            <div className="d-flex gap-2">
                                <Button
                                    as={Link}
                                    to="/login"
                                    variant="outline-info"
                                    className="font-monospace"
                                >
                                    {t('LOGIN')}
                                </Button>
                                <Button
                                    as={Link}
                                    to="/register"
                                    variant="outline-success"
                                    className="font-monospace"
                                >
                                    {t('REGISTER')}
                                </Button>
                            </div>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

function App() {
    const navigate = useNavigate();
    const [userRoles, setUserRoles] = useState([]);

    useEffect(() => {
        checkUserRoles();
    }, []);

    const checkUserRoles = () => {
        const token = localStorage.getItem('accessToken');

        if (!token) {
            setUserRoles([]);
            return;
        }

        try {
            const decoded = jwtDecode(token);
            const currentTime = Date.now() / 1000;

            if (decoded.exp < currentTime) {
                localStorage.clear();
                setUserRoles([]);
                return;
            }

            const authorities = decoded.authorities || [];
            setUserRoles(authorities);

            console.log('JWT authorities:', authorities);
        } catch (error) {
            console.error('JWT decode error:', error);
            setUserRoles([]);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        setUserRoles([]);
        navigate('/login');
    };

    return (
        <div className="bg-light min-vh-100">
            <AppNavbar userRoles={userRoles} onLogout={handleLogout} />
            <Container className="py-4 py-md-5">
                <Routes>
                    {/* Osnovne rute */}
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login onLoginSuccess={checkUserRoles} />} />
                    <Route path="/register" element={<Register />} />

                    {/* VEHICLES */}
                    <Route path="/vehicles" element={<VehicleList />} />
                    <Route path="/vehicles/add" element={<AddVehicle />} />
                    <Route path="/vehicles/edit/:id" element={<EditVehicle />} />

                    {/* SHIPMENTS */}
                    <Route path="/shipments" element={<ShipmentList />} />
                    <Route path="/shipments/new" element={<AddShipment />} />
                    <Route path="/shipments/details/:id" element={<ShipmentDetails />} />
                    <Route path="/shipments/edit/:id" element={<EditShipment />} />

                    {/* ASSIGNMENTS - Ovdje su bili tvoji glavni problemi */}
                    <Route path="/assignments" element={<AssignmentList />} />
                    <Route path="/assignments/new" element={<AddAssignment />} />
                    <Route path="/assignments/edit/:id" element={<EditAssignment />} /> {/* Popravljeno: maknut dupli AssignmentForm */}
                    <Route path="/assignments/:id" element={<AssignmentDetails />} />

                    {/* DRIVERS */}
                    <Route path="/drivers" element={<DriverList />} />
                    <Route path="/drivers/add" element={<DriverForm />} />
                    <Route path="/drivers/edit/:id" element={<DriverForm />} />
                    <Route path="/driver/dashboard" element={<DriverDashboard />} />
                    <Route path="/driver/assignment/:id" element={<DriverAssignmentDetails />} />

                    {/* OSTALO */}
                    <Route path="/analytics" element={<AnalyticsPage />} />
                    <Route path="/admin/users" element={<AdminDashboard />} />
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </Container>
        </div>
    );
}

AppNavbar.propTypes = {
    userRoles: PropTypes.array.isRequired,
    onLogout: PropTypes.func.isRequired
};

export default App;
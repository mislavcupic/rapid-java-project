// frontend/src/App.jsx
import React from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

// Import svih ruta/komponenti
import AddVehicle from './components/AddVehicle.jsx';
import VehicleList from './components/VehicleList.jsx';
import Login from './components/Login.jsx';
import Home from './components/Home.jsx';
import NotFound from './components/NotFound.jsx';
import EditVehicle from './components/EditVehicle.jsx';
import AssignmentList from './components/AssignmentList.jsx';
import AssignmentForm from './components/AssignmentForm.jsx';
import ShipmentList from './components/ShipmentList.jsx';
import ShipmentForm from './components/ShipmentForm.jsx';
import AddShipment from './components/AddShipment.jsx';
import EditAssignment from './components/EditAssignment.jsx';
import EditShipment from './components/EditShipment.jsx';
import AddAssignment from "./components/AddAssignment.jsx";
import DriverForm from "./components/DriverForm.jsx";
import DriverList from "./components/DriverList.jsx";
import AnalyticsPage from "./components/AnalyticsPage.jsx";
import Register from "./components/Register.jsx";

// =========================================================================
// NAVIGACIJSKA KOMPONENTA
// =========================================================================

const AppNavbar = () => {
    const { t, i18n } = useTranslation();
    const isAuthenticated = !!localStorage.getItem('accessToken');
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userRole');
        navigate('/login');
    };

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
                                <Nav.Link as={Link} to="/vehicles">{t('Vehicles')}</Nav.Link>
                                <Nav.Link as={Link} to="/drivers">{t('Drivers')}</Nav.Link>
                                <Nav.Link as={Link} to="/shipments">{t('Shipments')}</Nav.Link>
                                <Nav.Link as={Link} to="/assignments">{t('Assignments')}</Nav.Link>
                                <Nav.Link as={Link} to="/analytics">{t('Analytics')}</Nav.Link>
                            </>
                        )}
                    </Nav>

                    <Nav className="align-items-center">
                        {/* 🌐 Gumbi za promjenu jezika */}
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

                        {/* 🔐 Login / Register / Logout gumbi */}
                        {isAuthenticated ? (
                            <Button
                                onClick={handleLogout}
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
    return (
        <div className="bg-light min-vh-100">
            <AppNavbar />
            <Container className="py-4 py-md-5">
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/vehicles" element={<VehicleList />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="*" element={<NotFound />} />
                    <Route path="/vehicles/add" element={<AddVehicle />} />
                    <Route path="/vehicles/edit/:id" element={<EditVehicle />} />
                    <Route path="/assignments" element={<AssignmentList />} />
                    <Route path="/assignments/new" element={<AssignmentForm />} />
                    <Route path="/assignments/edit/:id" element={<AssignmentForm />} />
                    <Route path="/shipments" element={<ShipmentList />} />
                    <Route path="/shipments/new" element={<ShipmentForm />} />
                    <Route path="/shipments/edit/:id" element={<ShipmentForm />} />
                    <Route path="/shipments/add" element={<AddShipment />} />
                    <Route path="/assignments/edit/:id" element={<EditAssignment />}/>
                    <Route path="/assignments/add/:id" element={<AddAssignment />}/>
                    <Route path="/shipments/edit/:id" element={<EditShipment />}/>
                    <Route path="/drivers" element={<DriverList />} />
                    <Route path="/drivers/add" element={<DriverForm />} />
                    <Route path="/drivers/edit/:id" element={<DriverForm />} />
                    <Route path="/analytics" element={<AnalyticsPage />} />
                </Routes>
            </Container>
        </div>
    );
}

export default App;

// frontend/src/App.jsx (React Bootstrap Verzija)
import React from 'react';
import { Routes, Route, Link, useNavigate, BrowserRouter as Router } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap'; // Uvezi Bootstrap komponente
import AddVehicle from './components/AddVehicle.jsx';
// Import svih komponenti (.jsx)
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
const AppNavbar = () => {
    const isAuthenticated = !!localStorage.getItem('accessToken');
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        navigate('/login');
    };

    return (
        // Navbar s tamnom pozadinom (bg-dark) i elegantnom sjenom (shadow)
        <Navbar bg="dark" variant="dark" expand="sm" sticky="top" className="shadow-lg">
            <Container fluid className="px-md-5">
                {/* Logo - Monospace font i pastelni akcent */}
                <Navbar.Brand as={Link} to="/" className="font-monospace text-info fs-4 fw-bold">
                    FLEET.IO
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    {/* LIJEVA STRANA: Navigacijski linkovi */}
                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/">Početna</Nav.Link>
                        {isAuthenticated && (
                            <>
                                <Nav.Link as={Link} to="/vehicles">Vozila</Nav.Link>
                                <Nav.Link as={Link} to="/shipments">Pošiljke</Nav.Link>
                                <Nav.Link as={Link} to="/assignments">Dodjele</Nav.Link>
                            </>
                        )}
                    </Nav>

                    {/* DESNA STRANA: Prijavi se / Odjavi se */}
                    <Nav>
                        {isAuthenticated ? (
                            <Button
                                onClick={handleLogout}
                                variant="outline-danger" // Outlined gumb za Odjavu
                                className="font-monospace"
                            >
                                Odjava
                            </Button>
                        ) : (
                            <Button
                                as={Link}
                                to="/login"
                                variant="outline-info" // Pastelni outlined gumb za Prijavu
                                className="font-monospace"
                            >
                                Prijava
                            </Button>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

// Glavna App komponenta
function App() {
    return (
        <Router>
            <div className="bg-light min-vh-100"> {/* Svijetlo siva pozadina */}
                <AppNavbar />
                {/* max-w-7xl mx-auto py-8 sm:px-6 lg:px-8 zamjenjujemo s Containerom */}
                <Container className="py-4 py-md-5">
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/vehicles" element={<VehicleList />} />
                        <Route path="/login" element={<Login />} />
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
                    </Routes>
                </Container>
            </div>
        </Router>
    );
}

export default App;
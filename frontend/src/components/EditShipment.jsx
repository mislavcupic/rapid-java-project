// frontend/src/components/EditShipment.jsx

import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Form, Button, Card, Alert, Container, FloatingLabel, Spinner } from 'react-bootstrap';

// =================================================================
// 🛑 REACT LEAFLET UVEZ
// =================================================================
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { geocodeAddress } from '../services/ShipmentApi';

// 🛑 KRITIČNO: EKSPLICITAN UVOZ IKONA ZA LEAFLET (Rješava problem s putanjom)
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';


// ✅ ISPRAVAK: Uvoz shipment funkcija iz ShipmentApi.js
import { updateShipment, fetchShipmentById } from '../services/ShipmentApi';

// ✅ ISPRAVAK: Uvoz fetchDrivers i fetchVehicles iz VehicleApi.js
import { fetchDrivers, fetchVehicles } from '../services/VehicleApi';
import { useTranslation } from 'react-i18next';

// =================================================================
// FIKSIRANJE IKONA (Kopirano iz AddShipment.jsx)
// =================================================================
const customIcon = new L.Icon({
    // 🛑 Korištenje uvezenih resursa umjesto relativne putanje
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon2x,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// DEFAULT KOORDINATE i ZUM
const DEFAULT_COORDS = [45.815, 15.98];
const DEFAULT_ZOOM = 7;

// =================================================================
// 🛠 DINAMIČKA KOMPONENTA ZA PROMJENU POGLEDA KARTE (Kopirano iz AddShipment.jsx)
// =================================================================
function ChangeView({ center, zoom, bounds }) {
    const map = useMap();

    useEffect(() => {
        if (bounds) {
            map.fitBounds(bounds, { padding: [50, 50] });
        } else if (center) {
            map.setView(center, zoom);
        }
    }, [map, center, zoom, bounds]);

    return null;
}


const EditShipment = () => {
    const { t } = useTranslation();
    const { id } = useParams(); // ID pošiljke koju uređujemo
    const navigate = useNavigate();

    // 🛑 REFERENCA ZA PRISTUP LEAFLET OBJEKTU
    const mapRef = useRef(null);

    const [loading, setLoading] = useState(true); // Za učitavanje postojećih podataka
    const [saving, setSaving] = useState(false); // Za spremanje promjena
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const [drivers, setDrivers] = useState([]); // Lista dostupnih vozača
    const [vehicles, setVehicles] = useState([]); // Lista dostupnih vozila

    // Stanja za kartu
    const [pickupCoords, setPickupCoords] = useState(null);
    const [deliveryCoords, setDeliveryCoords] = useState(null);
    const [mapCenter, setMapCenter] = useState(DEFAULT_COORDS);
    const [mapZoom, setMapZoom] = useState(DEFAULT_ZOOM);
    const [mapBounds, setMapBounds] = useState(null);
    const [mapKey, setMapKey] = useState(0); // Ključ za prisilno ponovno montiranje karte


    // Polja DTO-u za pošiljku (ShipmentRequest)
    const [formData, setFormData] = useState({
        originAddress: '',
        destinationAddress: '',
        status: '',
        weightKg: '',
        // KRITIČNO: Dodana polja za Driver i Vehicle ID
        assignedDriverId: '',
        assignedVehicleId: '',
    });

    // =================================================================
    // 🛑 INVALDATESIZE HOOK (Reagira na promjenu adrese/reset ključa)
    // =================================================================
    useEffect(() => {
        if (mapRef.current) {
            // Dajemo mu malo više vremena (1000ms) za Bootstrap layout da se stabilizira
            setTimeout(() => {
                mapRef.current.invalidateSize();
            }, 1000);
        }
    }, [mapKey]);


    // =================================================================
    // EFFECT: GEOKODIRANJE (Resetira kartu)
    // =================================================================
    useEffect(() => {
        const debounceTimer = setTimeout(async () => {
            // Koristimo adrese iz formData
            const newPickupCoords = await geocodeAddress(formData.originAddress);
            const newDeliveryCoords = await geocodeAddress(formData.destinationAddress);

            setPickupCoords(newPickupCoords);
            setDeliveryCoords(newDeliveryCoords);

            // LOGIKA CENTRIRANJA I ZUMIRANJA
            if (newPickupCoords && newDeliveryCoords) {
                const bounds = new L.LatLngBounds([
                    [newPickupCoords.lat, newPickupCoords.lng],
                    [newDeliveryCoords.lat, newDeliveryCoords.lng]
                ]);
                setMapBounds(bounds);
                setMapCenter(bounds.getCenter().toArray());
                setMapZoom(DEFAULT_ZOOM);
            } else if (newPickupCoords) {
                setMapBounds(null);
                setMapCenter([newPickupCoords.lat, newPickupCoords.lng]);
                setMapZoom(12);
            } else if (newDeliveryCoords) {
                setMapBounds(null);
                setMapCenter([newDeliveryCoords.lat, newDeliveryCoords.lng]);
                setMapZoom(12);
            } else {
                setMapBounds(null);
                setMapCenter(DEFAULT_COORDS);
                setMapZoom(DEFAULT_ZOOM);
            }

            // 🛑 Ažuriraj ključ za ponovno montiranje/invalidateSize
            setMapKey(prev => prev + 1);

        }, 800);

        return () => clearTimeout(debounceTimer);

    }, [formData.originAddress, formData.destinationAddress]); // Reagira na promjenu adresa


    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Učitavanje liste vozača, vozila i podataka o pošiljci pri renderu
    useEffect(() => {
        const loadData = async () => {
            if (!localStorage.getItem('accessToken')) {
                setLoading(false);
                return navigate('/login');
            }
            try {
                // 1. Dohvaćanje listi za Dropdown (Sada rade jer je uvoz ispravan)
                const driverList = await fetchDrivers();
                setDrivers(driverList);
                const vehicleList = await fetchVehicles();
                setVehicles(vehicleList);

                // 2. Dohvaćanje postojećih podataka pošiljke
                const shipmentData = await fetchShipmentById(id);

                // KRITIČNO: Mapiranje polja iz ShipmetResponse DTO-a u formData
                setFormData({
                    originAddress: shipmentData.originAddress,
                    destinationAddress: shipmentData.destinationAddress,
                    status: shipmentData.status,
                    weightKg: shipmentData.weightKg,
                    // Mapiranje ID-jeva (Ako su null/undefined u backend respons-u, koristimo prazan string)
                    assignedDriverId: shipmentData.assignedDriverId || '',
                    assignedVehicleId: shipmentData.assignedVehicleId || '',
                });

                setLoading(false);
            } catch (err) {
                console.error("Greška pri učitavanju podataka:", err);
                // Provjera je li greška 403 (pristup odbijen)
                setError(err.message || t("error.general_error"));
                setLoading(false);
            }
        };
        loadData();
    }, [id, navigate, t]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);

        try {
            await updateShipment(id, formData);
            setSuccess(t('messages.shipment_updated'));
            setTimeout(() => navigate('/shipments'), 1500);

        } catch (err) {
            console.error("Greška pri spremanju:", err);
            setError(err.message || t("messages.update_failed"));
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="info" role="status" />
                <p className="text-muted mt-2">{t("general.loading_data")}</p>
            </div>
        );
    }

    return (
        <Container style={{ maxWidth: '700px' }}>
            <Card className="shadow-lg border-info border-top-0 border-5 p-4">
                <Card.Body>
                    <h2 className="text-info fw-bold font-monospace">
                        {t('forms.edit_shipment_title', { id })}
                    </h2>
                    {error && <Alert variant="danger" className="font-monospace">{error}</Alert>}
                    {success && <Alert variant="success" className="font-monospace">{success}</Alert>}

                    <Form onSubmit={handleSubmit} className="mt-4">

                        {/* 1. Opća polja pošiljke */}
                        <div className="row g-3 mb-4">
                            <div className="col-md-6">
                                <FloatingLabel controlId="originAddress" label={t("shipments.origin")}>
                                    <Form.Control type="text" name="originAddress" value={formData.originAddress} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                                {/* 🛑 KOORDINATE MORAJU BITI IZVAN FloatingLabel */}
                                {pickupCoords && (
                                    <Form.Text className="text-success">
                                        Pronađeno: Lat {pickupCoords.lat}, Lng {pickupCoords.lng}
                                    </Form.Text>
                                )}
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="destinationAddress" label={t("shipments.destination")}>
                                    <Form.Control type="text" name="destinationAddress" value={formData.destinationAddress} onChange={handleChange} required className="font-monospace" />
                                </FloatingLabel>
                                {/* 🛑 KOORDINATE MORAJU BITI IZVAN FloatingLabel */}
                                {deliveryCoords && (
                                    <Form.Text className="text-success">
                                        Pronađeno: Lat {deliveryCoords.lat}, Lng {deliveryCoords.lng}
                                    </Form.Text>
                                )}
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="weightKg" label={t("shipments.weight")}>
                                    <Form.Control type="number" name="weightKg" value={formData.weightKg} onChange={handleChange} required min="1" className="font-monospace" />
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="status" label={t("assignments.status")}>
                                    <Form.Select name="status" value={formData.status} onChange={handleChange} required className="font-monospace">
                                        <option value="">Odaberite status</option>
                                        <option value="PENDING">PENDING</option>
                                        <option value="IN_TRANSIT">IN_TRANSIT</option>
                                        <option value="DELIVERED">DELIVERED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                        </div>

                        {/* ================================================================= */}
                        {/* 🛑 INTEGRIRANA KARTA (MapContainer) */}
                        {/* ================================================================= */}
                        <div className="mb-4" style={{ border: '1px solid #ccc', overflow: 'visible' }}>
                            <h5 className="p-2 text-center bg-light">Vizualizacija Rute (React Leaflet)</h5>

                            {/* 🛑 PRISILNO RESETIRANJE (key={mapKey}) */}
                            <MapContainer
                                key={mapKey}
                                id="leaflet-map-kontejner" // KLJUČNI ID za CSS fiksiranje
                                className="leaflet-kontejner-fix" // KLASA ZA FORSIRANJE VISINE PREKO CSS-a
                                center={mapCenter}
                                zoom={mapZoom}
                                scrollWheelZoom={true}
                                ref={mapRef} // Referenca na Mapu za invalidateSize()

                                // DODATNA GARANCIJA: Poziv invalidateSize odmah nakon kreiranja
                                whenCreated={map => {
                                    setTimeout(() => {
                                        map.invalidateSize();
                                    }, 500);
                                }}
                            >
                                <ChangeView center={mapCenter} zoom={mapZoom} bounds={mapBounds} />

                                {/* KORIŠTENJE STABILNOG TILE SERVERA (CartoDB) */}
                                <TileLayer
                                    attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
                                    url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                    subdomains='abcd'
                                />

                                {pickupCoords && (
                                    <Marker position={[pickupCoords.lat, pickupCoords.lng]} icon={customIcon}>
                                        <Popup>{t('Polazište' )}</Popup>
                                    </Marker>
                                )}

                                {deliveryCoords && (
                                    <Marker position={[deliveryCoords.lat, deliveryCoords.lng]} icon={customIcon}>
                                        <Popup>{t('Odredište')}</Popup>
                                    </Marker>
                                )}
                            </MapContainer>
                        </div>
                        {/* ================================================================= */}


                        {/* 2. DODJELA VOZAČA I VOZILA */}
                        <hr className="my-4"/>
                        <h5 className="text-muted font-monospace">Dodjela</h5>
                        <div className="row g-3 mb-4">
                            <div className="col-md-6">
                                <FloatingLabel controlId="assignedDriverId" label={t("forms.assigned_driver")}>
                                    <Form.Select name="assignedDriverId" value={formData.assignedDriverId} onChange={handleChange} className="font-monospace">
                                        <option value="">Nije dodijeljen</option>
                                        {drivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {driver.firstName} {driver.lastName} ({driver.email})
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                            <div className="col-md-6">
                                <FloatingLabel controlId="assignedVehicleId" label={t("forms.assigned_vehicle")}>
                                    <Form.Select name="assignedVehicleId" value={formData.assignedVehicleId} onChange={handleChange} className="font-monospace">
                                        <option value="">{t("vehicles.unassigned")}</option>
                                        {vehicles.map(vehicle => (
                                            <option key={vehicle.id} value={vehicle.id}>
                                                {vehicle.make} {vehicle.model} - {vehicle.licensePlate}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </FloatingLabel>
                            </div>
                        </div>


                        <Button
                            type="submit"
                            variant="outline-success"
                            className="w-100 fw-bold font-monospace"
                            disabled={saving}
                        >
                            {saving ? (
                                <Spinner as="span" animation="border" size="sm"  aria-hidden="true" className="me-2" />
                            ) : (
                                t("general.save_changes")
                            )}
                        </Button>
                        <Button
                            variant="outline-secondary"
                            className="w-100 fw-bold font-monospace mt-2"
                            onClick={() => navigate('/shipments')}
                        >
                            {t("general.cancel")}
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default EditShipment;

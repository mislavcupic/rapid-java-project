import React, { useState, useEffect, useRef } from 'react'; // 🛑 DODAN useRef
import { Container, Card, Button, Form, Row, Col } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

// =================================================================
// 🛑 REACT LEAFLET UVEZ
// =================================================================
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { geocodeAddress } from '../services/ShipmentApi';

// =================================================================
// FIKSIRANJE IKONA
// =================================================================
const customIcon = new L.Icon({
    iconUrl: '/images/marker-icons/marker-icon.png',
    iconRetinaUrl: '/images/marker-icons/marker-icon-2x.png',
    shadowUrl: '/images/marker-icons/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// DEFAULT KOORDINATE i ZUM
const DEFAULT_COORDS = [45.815, 15.98];
const DEFAULT_ZOOM = 7;

// =================================================================
// 🛠 DINAMIČKA KOMPONENTA ZA PROMJENU POGLEDA KARTE
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

const AddShipment = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    // 🛑 REFERENCA ZA PRISTUP LEAFLET OBJEKTU
    const mapRef = useRef(null);

    // 1. STANJA
    const [pickupAddress, setPickupAddress] = useState('Ilica 2, Zagreb');
    const [deliveryAddress, setDeliveryAddress] = useState('Glavna ulica 114, Sesvete');
    const [pickupCoords, setPickupCoords] = useState({ lat: 45.8133, lng: 15.9689 }); // Postavljene default koordinate
    const [deliveryCoords, setDeliveryCoords] = useState({ lat: 43.5124, lng: 16.441 }); // Postavljene default koordinate

    // Stanja za kontrolu karte
    const [mapCenter, setMapCenter] = useState(DEFAULT_COORDS);
    const [mapZoom, setMapZoom] = useState(DEFAULT_ZOOM);
    const [mapBounds, setMapBounds] = useState(null);

    // 🛑 KLJUČ ZA PRISILNO PONOVO MONTIRANJE KARTE
    const [mapKey, setMapKey] = useState(0);

    // 🛑 INVALDATESIZE HOOK (Reagira na promjenu adrese/reset ključa)
    // Povećan timeout na 1000ms da se Bootstrap layout sigurno stabilizira
    useEffect(() => {
        if (mapRef.current) {
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
            const newPickupCoords = await geocodeAddress(pickupAddress);
            const newDeliveryCoords = await geocodeAddress(deliveryAddress);

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

    }, [pickupAddress, deliveryAddress]);


    // =================================================================
    // RENDER METODA
    // =================================================================
    return (
        <Container>
            <Card className="shadow-lg p-4">
                {/* 🛑 FORSIRANJE POZICIONIRANJA NA CARD.BODY */}
                <Card.Body style={{ position: 'relative' }}>
                    <h2 className="text-info fw-bold font-monospace">{t("forms.create_shipment_title")}</h2>

                    <Form className="mb-4">
                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Adresa Polazišta</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={pickupAddress}
                                        onChange={(e) => setPickupAddress(e.target.value)}
                                    />
                                    {pickupCoords && (
                                        <Form.Text className="text-success">
                                            Pronađeno: Lat {pickupCoords.lat}, Lng {pickupCoords.lng}
                                        </Form.Text>
                                    )}
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Adresa Odredišta</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={deliveryAddress}
                                        onChange={(e) => setDeliveryAddress(e.target.value)}
                                    />
                                    {deliveryCoords && (
                                        <Form.Text className="text-success">
                                            Pronađeno: Lat {deliveryCoords.lat}, Lng {deliveryCoords.lng}
                                        </Form.Text>
                                    )}
                                </Form.Group>
                            </Col>
                        </Row>
                        {/* Ostali inputi... */}
                    </Form>

                    {/* 4. KONTEJNER ZA REACT LEAFLET KARTU */}
                    {/* 🛑 FORSIRANJE OVERFLOW: VISIBLE NA RODITELJSKOM DIV-U */}
                    <div className="mb-4" style={{ border: '1px solid #ccc', overflow: 'visible' }}>
                        <h5 className="p-2 text-center bg-light">Vizualizacija Rute (React Leaflet)</h5>

                        {/* 🛑 PRISILNO RESETIRANJE (key={mapKey}) */}
                        <MapContainer
                            key={mapKey}
                            id="leaflet-map-kontejner" // KLJUČNI ID za CSS fiksiranje
                            className="leaflet-kontejner-fix" // 🛑 DODANA KLASA ZA FORSIRANJE VISINE PREKO CSS-a
                            center={mapCenter}
                            zoom={mapZoom}
                            scrollWheelZoom={true}
                            ref={mapRef} // 🛑 Referenca na Mapu za invalidateSize()

                            // 🛑 DODATNA GARANCIJA: Poziv invalidateSize odmah nakon kreiranja
                            whenCreated={map => {
                                // Povećan timeout na 500ms za invalidateSize() pri prvom kreiranju
                                setTimeout(() => {
                                    map.invalidateSize();
                                }, 500);
                            }}

                            // UKLONJEN INLINE STYLE: CSS (index.css) preuzima kontrolu!
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
                                    <Popup>{t('Polazište')}</Popup>
                                </Marker>
                            )}

                            {deliveryCoords && (
                                <Marker position={[deliveryCoords.lat, deliveryCoords.lng]} icon={customIcon}>
                                    <Popup>{t('Odredište')}</Popup>
                                </Marker>
                            )}
                        </MapContainer>
                    </div>

                    {/* Kontrolni gumbi */}
                    <Button variant="primary" className="me-2" onClick={() => {/* Logika za Kreiranje Pošiljke */}}>
                        Kreiraj Pošiljku
                    </Button>
                    <Button variant="secondary" onClick={() => navigate('/shipments')}>
                        Odustani
                    </Button>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AddShipment;
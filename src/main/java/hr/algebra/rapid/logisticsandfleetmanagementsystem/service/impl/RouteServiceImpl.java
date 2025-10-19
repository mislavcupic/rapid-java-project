// hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.RouteServiceImpl.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RouteStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService;
import org.springframework.stereotype.Service;

/**
 * Implementacija RouteService sučelja koja proračunava udaljenost između dvije točke.
 */
@Service
public class RouteServiceImpl implements RouteService {

    /**
     * Implementacija metode za proračun rute.
     * U stvarnom svijetu, ovdje bi bio poziv prema vanjskom API-ju (npr. Google Distance Matrix).
     */
    @Override
    public Route calculateAndCreateRoute(String originAddress, Double oLat, Double oLon,
                                         String destinationAddress, Double dLat, Double dLon) {

        Route newRoute = new Route();

        // 1. Postavljanje adresa i koordinata
        newRoute.setOriginAddress(originAddress);
        newRoute.setOriginLatitude(oLat);
        newRoute.setOriginLongitude(oLon);

        newRoute.setDestinationAddress(destinationAddress);
        newRoute.setDestinationLatitude(dLat);
        newRoute.setDestinationLongitude(dLon);

        // 2. Logika proračuna (Simulacija vanjskog API-ja)
        double fiktivnaUdaljenost = calculateHaversineDistance(oLat, oLon, dLat, dLon);
        long fiktivnoTrajanje = (long) (fiktivnaUdaljenost * 1.5); // Pretpostavka: 1.5 min po km

        // Zaokruživanje udaljenosti na dvije decimale
        newRoute.setEstimatedDistanceKm(Math.round(fiktivnaUdaljenost * 100.0) / 100.0);
        newRoute.setEstimatedDurationMinutes(fiktivnoTrajanje);

        // 3. Postavljanje statusa
        newRoute.setStatus(RouteStatus.CALCULATED);

        return newRoute;
    }

    /**
     * Pomoćna funkcija za proračun udaljenosti na Zemlji (Haversine formula).
     * Koristi se za simulaciju cestovne udaljenosti (daje zračnu liniju).
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radijus Zemlje u km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
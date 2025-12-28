package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RouteStatus;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService;
import org.springframework.stereotype.Service;

@Service
public class RouteServiceImpl implements RouteService {

    @Override
    public Route calculateAndCreateRoute(String originAddress, Double oLat, Double oLon,
                                         String destinationAddress, Double dLat, Double dLon) {

        Route newRoute = new Route();

        // 1. Postavljanje adresa i koordinata (OVO JE KLJUČNO ZA TVOJU MAPU)
        newRoute.setOriginAddress(originAddress);
        newRoute.setOriginLatitude(oLat);
        newRoute.setOriginLongitude(oLon);

        newRoute.setDestinationAddress(destinationAddress);
        newRoute.setDestinationLatitude(dLat);
        newRoute.setDestinationLongitude(dLon);

        // 2. Logika proračuna udaljenosti (Haversine formula)
        double distance = 0.0;
        if (oLat != null && oLon != null && dLat != null && dLon != null) {
            distance = calculateHaversineDistance(oLat, oLon, dLat, dLon);
        }

        long duration = (long) (distance * 1.2); // Simulacija: 1.2 min po km

        newRoute.setEstimatedDistanceKm(Math.round(distance * 100.0) / 100.0);
        newRoute.setEstimatedDurationMinutes(duration);

        // 3. Postavljanje statusa
        newRoute.setStatus(RouteStatus.CALCULATED);

        return newRoute;
    }

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
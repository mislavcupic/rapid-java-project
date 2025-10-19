// hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;

/**
 * Sučelje za servis koji se bavi proračunom i upravljanjem rutama.
 */
public interface RouteService {

    /**
     * Proračunava rutu na temelju polazišnih i odredišnih koordinata
     * i kreira novi Route entitet s proračunatom udaljenošću i trajanjem.
     *
     * @param originAddress Adresa polazišta
     * @param oLat Latituda polazišta
     * @param oLon Longituda polazišta
     * @param destinationAddress Adresa odredišta
     * @param dLat Latituda odredišta
     * @param dLon Longituda odredišta
     * @return Novo kreirani Route objekt s proračunatim podacima
     */
    Route calculateAndCreateRoute(String originAddress, Double oLat, Double oLon,
                                  String destinationAddress, Double dLat, Double dLon);
}
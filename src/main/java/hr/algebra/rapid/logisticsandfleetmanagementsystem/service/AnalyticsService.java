package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;

public interface AnalyticsService {

    // Analitika: Dohvaća prosječnu težinu aktivnih pošiljaka (PENDING ili IN_TRANSIT)
    Double getAverageActiveShipmentWeight();

    // Bulk Operacija: Ažurira status pošiljaka kojima je istekao očekivani rok isporuke
    int bulkMarkOverdue();
}

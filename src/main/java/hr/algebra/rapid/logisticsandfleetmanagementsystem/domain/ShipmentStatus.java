// hr.algebra.rapid.logisticsandfleetmanagementsystem.model.ShipmentStatus.java

package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

/**
 * Definicija mogućih statusa pošiljke.
 * Pomaže u osiguravanju dosljednosti podataka u bazi.
 */
public enum ShipmentStatus {

    // Inicijalni statusi
    PENDING,        // Čeka dodjelu i zakazivanje
    SCHEDULED,      // Dodijeljena vozaču i vozilu, čeka početak

    // Statusi u tranzitu
    IN_TRANSIT,     // Na putu do odredišta
    OVERDUE,        // Istečen očekivani rok isporuke (postavlja Scheduler/JdbcTemplate)

    // Završni statusi
    DELIVERED,      // Uspješno isporučeno
    DELAYED, CANCELED        // Otkazano
}


package hr.algebra.rapid.logisticsandfleetmanagementsystem.domain;

public enum RouteStatus {
    // RUTA je definirana, ali još nije dodijeljena/pokrenuta
    DRAFT,

    // RUTA je proračunata, spremna za dodjelu
    CALCULATED,

    // RUTA je aktivna, vozač je na terenu
    ACTIVE,

    // RUTA je završena
    COMPLETED,

    // RUTA je otkazana
    CANCELED
}
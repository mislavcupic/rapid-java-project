package hr.algebra.rapid.logisticsandfleetmanagementsystem.repository;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    // NIŠTA - JpaRepository daje sve CRUD metode automatski!
    // save(), findById(), findAll(), delete(), itd.
}
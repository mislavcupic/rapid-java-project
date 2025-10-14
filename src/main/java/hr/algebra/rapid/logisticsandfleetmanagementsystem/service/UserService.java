package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;// src/main/java/hr/algebra/rapid/logisticsandfleetmanagementsystem/service/UserService.java

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.ApplicationUser;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;

import java.util.List;

public interface UserService {

    List<UserInfo> findAll();

    UserInfo findById(Long id); // Važna za dodjelu vozača vozilu
}

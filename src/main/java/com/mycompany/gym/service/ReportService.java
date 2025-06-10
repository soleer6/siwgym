// src/main/java/com/mycompany/gym/service/ReportService.java
package com.mycompany.gym.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mycompany.gym.domain.Registration;
import com.mycompany.gym.repository.RegistrationRepository;

@Service
public class ReportService {

    @Autowired
    private RegistrationRepository registrationRepo;

    /**
     * Devuelve el total de ingresos (sumatorio de pricePaid de todas las inscripciones).
     */
    public double getTotalRevenue() {
        return registrationRepo.findAll()
                .stream()
                .mapToDouble(Registration::getPricePaid)
                .sum();
    }

    /**
     * Devuelve un Map donde la clave es "YYYY-MM" y el valor es el ingreso total en ese mes.
     * Ejemplo: { "2025-06" : 150.0, "2025-07" : 200.0, ... }
     */
    public Map<String, Double> getMonthlyRevenue() {
        List<Registration> all = registrationRepo.findAll();
        return all.stream().collect(Collectors.groupingBy(
            r -> {
                int year = r.getTimestamp().getYear();
                int month = r.getTimestamp().getMonthValue();
                return String.format("%d-%02d", year, month);
            },
            Collectors.summingDouble(Registration::getPricePaid)
        ));
    }

    /**
     * Devuelve un Map donde la clave es el nombre de la actividad
     * y el valor es el ingreso total generado por esa actividad.
     */
    public Map<String, Double> getRevenueByActivity() {
        List<Registration> all = registrationRepo.findAll();
        return all.stream().collect(Collectors.groupingBy(
            r -> r.getActivity().getName(),
            Collectors.summingDouble(Registration::getPricePaid)
        ));
    }
}

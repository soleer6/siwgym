// src/main/java/com/mycompany/gym/controller/ReportController.java
package com.mycompany.gym.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.gym.service.ReportService;
import static com.mycompany.gym.util.SecurityUtil.isAdmin;
import static com.mycompany.gym.util.SecurityUtil.isLoggedIn;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /** Total acumulado de ingresos. Solo accesible a ADMIN. */
    @GetMapping("/total")
    public ResponseEntity<?> getTotalRevenue(HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Access denied: ADMIN only.");
        }
        double total = reportService.getTotalRevenue();
        return ResponseEntity.ok(total);
    }

    /** Ingresos por mes (formato "YYYY-MM" → valor). Solo ADMIN. */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyRevenue(HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Access denied: ADMIN only.");
        }
        Map<String, Double> map = reportService.getMonthlyRevenue();
        return ResponseEntity.ok(map);
    }

    /** Ingresos por actividad (nombreActividad → valor). Solo ADMIN. */
    @GetMapping("/by-activity")
    public ResponseEntity<?> getRevenueByActivity(HttpServletRequest request) {
        if (!isLoggedIn(request)) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body("Access denied: ADMIN only.");
        }
        Map<String, Double> map = reportService.getRevenueByActivity();
        return ResponseEntity.ok(map);
    }
}

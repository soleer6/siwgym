package com.mycompany.gym.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "max_capacity")
    private int maxCapacity;

    @Column(name = "discount_percentage")
    private Double discountPercentage; // e.g. 20.0 significa 20% off

    @Column(name = "offer_ends_at")
    private LocalDateTime offerEndsAt; // hasta cuándo dura la oferta

    private double price;
    private boolean active;


    // Relación ManyToOne a monitor (un monitor puede tener muchas actividades)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id")
    @JsonIgnoreProperties({ "activities", "password", "hibernateLazyInitializer" })
    private User monitor;
    

    // Relación ManyToMany con User (los clientes apuntados a la actividad)
    @ManyToMany
    @JoinTable(
        name = "activity_user",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({ "activities", "hibernateLazyInitializer" })
    private List<User> attendees = new ArrayList<>();

    // Lista de sesiones de esta actividad
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"activity", "hibernateLazyInitializer", "handler"})
    private List<Session> sessions = new ArrayList<>();

    public Activity() { 
        
    }

    public Activity(String name,
                    String description,
                    LocalDateTime dateTime,
                    int maxCapacity,
                    double price,
                    User monitor) {
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.maxCapacity = maxCapacity;
        this.price = price;
        this.monitor = monitor;
        this.active = true; // Por defecto la dejamos activa
    }
    // ======= Getters & Setters =======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }


    public User getMonitor() { return monitor; }
    public void setMonitor(User monitor) { this.monitor = monitor; }

    public List<User> getAttendees() { return attendees; }
    public void setAttendees(List<User> attendees) { this.attendees = attendees; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discount){
        this.discountPercentage = discount;
    }

    public LocalDateTime getOfferEndsAt() {
        return offerEndsAt;
    }

    public void setOfferEndsAt(LocalDateTime offerEndsAt) {
        this.offerEndsAt = offerEndsAt;
    }
}

package com.mycompany.gym.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;
    private String role;
    private boolean penalized;

    // Si quieres evitar que Jackson recorra la lista de actividades desde el User:
    @ManyToMany(mappedBy = "attendees")
    @JsonIgnore
    private List<Activity> activities = new ArrayList<>();

    // constructor vacío (requerido por JPA)
    public User() {}

    public User(String username, String password, String role, boolean penalized) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.penalized = penalized;
    }

    // ======= Getters & Setters ========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isPenalized() { return penalized; }
    public void setPenalized(boolean penalized) { this.penalized = penalized; }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }
}

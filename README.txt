# SIW Gym Management

A full-stack web application for managing gym activities, built with Spring Boot (Java) back-end and vanilla JavaScript/HTML front-end.

## Features

- **Role-Based Access**  
  - **Admin**: create/edit activities & sessions, apply offers, view revenue reports  
  - **Monitor**: view assigned activities & sessions (mini-calendar)  
  - **Client**: browse activities, view details, sign up  

- **Authentication & Security**  
  - Session-based login/register via `/api/auth/login` and `/api/auth/register`  
  - Passwords hashed with BCrypt  
  - Spring Security protects REST endpoints; CSRF disabled only on auth endpoints  

- **Activity Scheduling**  
  - Each activity consists of one or more sessions  
  - Sessions have individual room assignments and capacity enforcement  

- **Notifications**  
  - Automatic notifications on new activities and full sessions  
  - Managed via `/api/notifications`  

- **Offers & Revenue Reports**  
  - Admin can set percentage discounts with expiration  
  - Reports include total revenue, monthly breakdown, and revenue by activity  

## Tech Stack

- **Spring Boot** (Java)  
- **Spring Security** (HTTP sessions + BCrypt)  
- **Spring Data JPA** with H2 file-based DB (`data/siwgym.mv.db`)  
- **Vanilla JavaScript/HTML/CSS** front-end  

## Quick Start

1. **Clone & Run**  
   ```bash
   git clone https://github.com/your-username/siwgym.git
   cd siwgym
   ./mvnw spring-boot:run

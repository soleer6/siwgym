# Gym Management System

## Overview

A full-stack web application for managing gym activities, built with Spring Boot (Java) back-end and vanilla JavaScript/HTML front-end. Supports three user roles: Admin, Monitor, and Client.

## Key Features

* **Role-Based Access**: Admins manage activities and offers; Monitors oversee assigned activities; Clients browse, view, and sign up.
* **Activity Scheduling**: Create activities composed of multiple sessions, each with its own room.
* **Client Registration**: Clients can sign up for sessions until capacity is reached, with notifications.
* **Notifications**: Automated notifications for new activities, sign-ups, and full capacity.
* **Revenue Reporting**: Total and monthly revenue reports, plus revenue by activity.

## Architecture

* **Spring Boot**: REST API controllers for authentication, activities, sessions, offers, and reports.
* **JPA / Hibernate**: Entity mappings for User, Room, Activity, Session, Registration, Notification; H2 file-based persistence with `AUTO_SERVER`.
* **Spring Security**: HTTP session-based authentication and role checks.
* **Front-End**: HTML/CSS for structure and styling, and JavaScript for dynamic UI, form handling, and API calls.
s
## Default Credentials

* **Admin**: username `admin`, password `password`
* **Monitor**: `monitor1`, `monitor2`, `monitor3` / `password`
* **Client**: `client1`, `client2`, `client3` / `password`

## Project Structure

* `src/main/java/.../domain`: JPA entities.
* `src/main/java/.../repository`: Spring Data repositories.
* `src/main/java/.../service`: Business logic.
* `src/main/java/.../controller`: REST API endpoints.
* `src/main/resources/static`: HTML and JS front-end.

## Further Reading

* **Application.properties** configures H2, JPA, and security.
* **DataLoader** seeds initial data on startup.
* **SecurityConfig** defines access rules.


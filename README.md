# CoachBooking – Mobile Application

CoachBooking is a mobile application designed to simplify the booking process between fitness trainers and clients in gyms.  
The app allows trainers to manage their availability and clients to easily reserve training sessions.

The project focuses on modern Android development practices, clear business logic, and user-friendly design.

---

## Project Overview

The main idea of the application is to connect **clients**, **trainers**, and **gyms** in one system:

- Clients can browse gyms and trainers
- Trainers can create available training times
- Clients can book or cancel training sessions
- The system prevents double-booking of the same time slot

The application supports **two user roles**:
- **Client**
- **Trainer**

---

## System Architecture

The system follows a **client–backend architecture** using a **Backend-as-a-Service (BaaS)** approach.

### Architecture Components

- **Android Mobile Application**
  - Kotlin
  - Jetpack Compose
- **Backend Services**
  - Firebase Authentication
  - Cloud Firestore
  - Firestore Security Rules
    
        [ Android App ]
              |
              v
        [ Firebase Authentication ]
        [ Cloud Firestore Database ]


Firebase is used instead of a traditional server to reduce infrastructure complexity and focus on application logic and user experience.

---

## Server Architecture Description

### Backend Solution

The backend is implemented using **Firebase services**:

- **Firebase Authentication**
  - Handles user registration and login
- **Cloud Firestore**
  - Stores gyms, trainers, timeslots, and reservations
- **Security Rules**
  - Enforce role-based access (client / trainer)

### Data Model (Simplified)

- **Users**
  - uid
  - email
  - role (client / trainer)
  - name, surname
  - gymId (for trainers)

- **Gyms**
  - id
  - name
  - address

- **Timeslots**
  - id
  - trainerId
  - date
  - startTime
  - endTime
  - status (free / booked)

- **Reservations**
  - id
  - clientId
  - trainerId
  - timeslotId
  - date
  - status

---

## REST API Documentation (Conceptual)

Although Firebase does not use a traditional REST API, the application logic can be represented using REST-style endpoints:

| Method | Endpoint | Description |
|------|---------|-------------|
| POST | /auth/login | User login |
| POST | /auth/register | User registration |
| GET | /gyms | Get list of gyms |
| GET | /gyms/{id}/trainers | Get trainers by gym |
| GET | /trainers/{id}/timeslots | Get trainer availability |
| POST | /reservations | Create reservation |
| DELETE | /reservations/{id} | Cancel reservation |

These operations are implemented using Firebase SDK calls instead of HTTP requests.

---

## Authentication Methods

- **Email & Password authentication**
- Implemented using Firebase Authentication
- Secure session handling managed by Firebase

### User Roles

- **Client**
  - Browse gyms and trainers
  - Book and cancel training sessions

- **Trainer**
  - Create training timeslots
  - View client reservations

Access control is enforced using Firestore Security Rules.

---

## Mobile App Design

The application uses a **dark LemonGym-inspired theme**:

- Dark background suitable for gym-style apps
- Yellow accent color for primary actions
- Material 3 components
- Smooth animations and transitions

### Main Screens

- **Login / Register**
- **Client Home (Gym List)**
- **Trainer List**
- **Booking Screen**
- **Trainer Dashboard**
- **Reservations Screens**

---

## Screen Flow Diagram

    Login / Register
          |
          v
    Role Check
         | |
    Client Trainer
         | |
    Gym List Trainer Home
          |
    Trainer List
          |
    Booking Screen
          |
    Reservation Created

---

## Technical Implementation Details

### Android Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- Material 3
- State management with `remember` and `mutableStateOf`

### UI / UX Enhancements

- Animated list items (fade + slide-in)
- Press-scale click animations
- Responsive layouts
- Consistent dark theme

### Data Handling

- Real-time Firestore data updates
- Transaction-based reservation creation
- Prevention of double-booking

---

## Security Considerations

- Firebase Authentication for user identity
- Role-based access using Firestore Security Rules
- Users can only modify their own data

---

## Future Improvements

- Payment integration
- Trainer ratings and reviews
- Push notifications
- Migration to a custom REST backend if needed

---

## Conclusion

CoachBooking is a fully functional mobile application demonstrating modern Android development practices, real-world booking logic, and a clean user interface.

The project is scalable and can be extended with additional backend services or third-party integrations in the future.

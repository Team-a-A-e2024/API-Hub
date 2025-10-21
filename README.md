# README
## Setup

1. Clone project
2. Create database named `peli`

## Authors
**Project**: API-Hub <br>
**Course**: Datamatiker 3. semester <br>
**Education**: CPH Business Lyngby <br>

**Name**: Abbas M. Badreddine <br>
**EK e-mail**: cph-ab632@stud.ek.dk <br>
**Github**: AbbasMB <br>

**Name**: Emil Wolfert Schmidt Andersen <br>
**EK e-mail**: cph-ea178@stud.ek.dk <br>
**Github**: iirne <br>

**Name**: Frederik Bastiansen <br>
**EK e-mail**: cph-fb157@stud.ek.dk <br>
**Github**: Frederik-BipBop <br>

**Name**: Thomas Atchapero <br>
**EK e-mail**: cph-ta241@stud.ek.dk <br>
**Github**: Tatchapero <br>

---

## API Documentation

| Method | URL | Request Body (JSON) | Response (JSON) | Error (e) |
| --- | --- | --- | --- | --- |
| GET | /api/users | | [user, user, …] (1) | |
| GET | /api/users/{id} | | user (1) | (e1) |
| POST | /api/users	| user(1) without id | | (e2) |
| UPDATE | /api/users/{id} | user(1) without id | user (1) | |
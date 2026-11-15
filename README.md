# Animal Picture App

A small Spring Boot microservice that fetches random pictures of cats, dogs and bears
from public APIs, stores them in a database, and serves the most recently saved picture
for each animal. It includes a simple web UI and automated tests.

## Quick start

### Option 1: Docker (app + PostgreSQL)

Requirements: Docker with Docker Compose.

```bash
docker compose up --build
```

Then open http://localhost:8080.

Pictures are stored in PostgreSQL on a Docker volume, so they survive restarts.
To stop: `docker compose down` (add `-v` to also delete the stored pictures).

### Option 2: Without Docker (embedded H2 database)

Requirements: Java 21. Maven is **not** required, because the project includes the Maven Wrapper.

```bash
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run      # Windows
```

Then open http://localhost:8080.

This mode uses an in-memory H2 database, so nothing needs to be installed. Data is cleared when the app stops.

> Port 8080 must be free. Don't run both options at the same time.

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/pictures?animal={animal}&count={n}` | Fetches `n` random pictures (default 1, max 10) and saves them |
| `GET` | `/api/pictures/{animal}/latest` | Returns the most recently saved picture as an image |

`animal` is one of `cat`, `dog`, `bear` (case-insensitive).

### Examples

```bash
# Fetch and save 3 bear pictures
curl -X POST "http://localhost:8080/api/pictures?animal=bear&count=3"

# Download the latest bear picture
curl -o bear.jpg http://localhost:8080/api/pictures/bear/latest
```

Example response of the `POST` (status `201 Created`):

```json
[
  {
    "id": 1,
    "animalType": "BEAR",
    "sourceUrl": "https://placebear.com/312/245",
    "sizeBytes": 17715,
    "createdAt": "2026-11-15T10:00:00Z"
  }
]
```

### Error responses

| Status | When |
|---|---|
| `400 Bad Request` | Unknown animal, or `count` outside 1–10 |
| `404 Not Found` | No picture saved yet for that animal |
| `502 Bad Gateway` | The external picture provider failed or timed out |

## Web UI

Open http://localhost:8080, pick an animal and a count, then click **Fetch & save**
(it fetches the pictures and shows the latest one) or **Show latest**.

## Running the tests

```bash
./mvnw test
```

- `PictureServiceTest`: unit tests for the service, with the picture provider and database mocked.
- `PictureApiTest`: integration tests that start the full application with an H2 database
  and a mocked picture provider. They cover both endpoints, validation, 404 and 502 cases.

The tests never call the real external APIs, so they're fast, reliable and work offline.

## Design decisions

- **Picture sources:** placekitten.com is currently down (HTTP 521), so cats come from
  [cataas.com](https://cataas.com), an alternative listed in public-apis.
  Dogs come from place.dog and bears from placebear.com.
- **Store the image itself, not just the URL:** the image bytes are saved in the database,
  so stored pictures stay available even if the provider changes or goes offline.
- **Random pictures:** place.dog and placebear.com return the same image for the same size,
  so the app requests random dimensions to get different pictures.
- **All or nothing:** all requested pictures are fetched first and then saved in one batch.
  If one download fails, nothing is saved.
- **Resilience:** HTTP calls have connect and read timeouts, and provider failures return
  a clear `502` instead of a generic `500`.
- **Two database setups, one codebase:** H2 for a zero-install local run, PostgreSQL in Docker
  for a production-like setup. The database is selected through environment variables, with no code changes.
- **Container:** a multi-stage Docker build (JDK to build, smaller JRE image to run), and the app runs as a non-root user.

## Tech stack

Java 21 · Spring Boot 4 (Web, Data JPA, Validation, Actuator) · Maven Wrapper ·
H2 · PostgreSQL 17 · JUnit 5 · Mockito · Docker Compose

## Project structure

```
src/main/java/com/example/animalpictures/
├── PictureController.java          REST endpoints
├── PictureService.java             business logic (fetch N, save all)
├── AnimalPictureClient.java        calls the external picture APIs
├── PictureRepository.java          database access (Spring Data JPA)
├── Picture.java                    database entity
├── AnimalType.java                 CAT, DOG, BEAR
├── PictureResponse.java            API response (DTO)
├── FetchedPicture.java             downloaded picture before saving
├── StringToAnimalTypeConverter.java  case-insensitive animal names
└── ApiExceptionHandler.java        maps provider failures to 502
src/main/resources/static/index.html  simple web UI
```

## What I would do next

- **Gallery / picture by id:** add `GET /api/pictures/{id}` so the UI can show all pictures from one request, not only the latest.
- **Database migrations:** replace `ddl-auto=update` with Flyway for versioned, reviewable schema changes.
- **Configurable providers:** move the provider URLs and timeouts to `application.properties`.
- **More resilience:** retries with backoff and a circuit breaker (e.g. Resilience4j), and fetching multiple pictures in parallel.
- **Clearer validation errors:** return a helpful message such as "valid values are cat, dog, bear".
- **API documentation:** generate an OpenAPI / Swagger UI page.
- **CI:** a GitHub Actions workflow that runs the tests and builds the Docker image on every push.
- **Production readiness:** secrets from a secret manager instead of compose, a Helm chart for Kubernetes,
  and object storage (e.g. S3) for images at larger scale.

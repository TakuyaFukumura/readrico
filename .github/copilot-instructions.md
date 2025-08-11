# Readrico - Japanese Reading Record Management Application

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Project Overview

Readrico is a Spring Boot web application for managing reading records (読書記録). Users can track books they're reading, their progress, ratings, and thoughts. The application uses Java 17, Spring Boot 3.5.4, H2 in-memory database, Thymeleaf templating, and Bootstrap for the UI.

## Working Effectively

### Prerequisites and Environment Setup
- Java 17+ is required (OpenJDK 17.0.16 available in environment)
- Maven is managed via Maven Wrapper (./mvnw) - no separate Maven installation needed
- Docker available for containerized deployment

### Build Commands and Timing
**CRITICAL**: Never cancel Maven builds - they can take several minutes on first run due to dependency downloads.

- **Clean compile** (first time setup with downloads):
  ```bash
  ./mvnw clean compile
  ```
  - Takes approximately 3 minutes on first run (downloads dependencies)
  - NEVER CANCEL: Set timeout to 300+ seconds (5+ minutes)
  
- **Package build** (without tests):
  ```bash
  ./mvnw clean package -DskipTests
  ```
  - Takes approximately 1.5 minutes after initial dependency download
  - NEVER CANCEL: Set timeout to 180+ seconds (3+ minutes)

- **Full build with tests**:
  ```bash
  ./mvnw clean package
  ```
  - Tests complete in ~8-15 seconds (104 Spock tests)
  - Total build time: ~7-8 seconds after dependencies are cached
  - NEVER CANCEL: Set timeout to 180+ seconds (3+ minutes) to account for dependency downloads

### Running the Application

- **Via Maven (development)**:
  ```bash
  ./mvnw spring-boot:run
  ```
  - Application starts on port 8080
  - Startup takes ~3 seconds
  - Access at http://localhost:8080

- **Via JAR file**:
  ```bash
  java -jar target/readrico.jar
  ```
  - Must build first: `./mvnw clean package`
  - Same startup behavior as Maven

### Testing

- **Run all tests**:
  ```bash
  ./mvnw test
  ```
  - 104 tests using Spock framework (Groovy)
  - Takes 10-15 seconds to complete
  - All tests should pass

- **Test categories**: Service layer, controller layer, entity validation, status enums
- No additional linting or code formatting tools are configured

### Docker Deployment

- **Build Docker image** (requires pre-built JAR):
  ```bash
  ./mvnw clean package -DskipTests
  docker build -t readrico .
  ```
  - Docker build takes ~3 seconds after JAR is ready
  - Uses Eclipse Temurin 21 JRE Alpine base image

- **Docker Compose deployment**:
  ```bash
  docker compose up --build
  ```
  - Builds and starts application
  - Takes ~3 seconds total
  - Access at http://localhost:8080

- **Stop Docker Compose**:
  ```bash
  docker compose down
  ```

## Validation

### Manual Testing Requirements
**ALWAYS** perform these validation steps after making changes:

1. **Application startup verification**:
   - Start application and verify it reaches "Started Main in X.X seconds"
   - Check http://localhost:8080 returns homepage HTML with title "Readrico - 読書記録アプリ"
   - Verify http://localhost:8080/reading-records shows reading records interface

2. **Core functionality testing**:
   - Navigate to reading records list: http://localhost:8080/reading-records
   - Verify tabs show different reading statuses (未読, 読書中, 読了, 中止) with counts
   - Check sample data loads correctly (should show Japanese book titles like "銀河鉄道の夜", "雪国")
   - Test new record creation: http://localhost:8080/reading-records/new (should show title "読書記録登録")
   - Verify H2 console accessible: http://localhost:8080/h2-console (redirects to login page)

3. **Database connectivity testing**:
   - H2 Console URL: http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:testdb
   - Username: sa
   - Password: (blank)
   - Should show reading_record table with sample data

4. **Build artifact verification**:
   - Check JAR file is created: `ls -lh target/readrico.jar` (should be ~58MB)
   - Verify JAR can run: `java -jar target/readrico.jar` (starts Spring Boot app)

5. **Test execution verification**:
   - Always run `./mvnw test` before final validation
   - Verify all 104 tests pass (Spock framework)
   - Look for any new test failures related to your changes

### CI/CD Validation
The GitHub Actions workflow (`.github/workflows/build.yml`) runs:
- Java 17 setup with Amazon Corretto
- Maven build: `mvn clean package`
- Always ensure your changes don't break the CI build

## Key Application Features

### Reading Status Management
- **UNREAD** (未読): Books not yet started
- **READING** (読書中): Currently reading books  
- **COMPLETED** (読了): Finished books
- **PAUSED** (中止): Books on hold/discontinued

### Core Functionality
- Add/edit/delete reading records
- Track reading progress (current page / total pages)
- Record book ratings (1-5 scale)
- Add thoughts and summaries
- CSV export/import functionality
- Dark mode toggle

## Project Structure

### Source Code Organization
```
src/main/java/com/example/myapplication/
├── Main.java                          # Application entry point
├── controller/                        # Web controllers
│   ├── IndexController.java          # Homepage
│   └── ReadingRecordController.java  # CRUD operations
├── entity/                           # JPA entities
│   └── ReadingRecord.java           # Main data model
├── repository/                       # Data access layer
│   └── ReadingRecordRepository.java # JPA repository
├── service/                          # Business logic
│   └── ReadingRecordService.java    # Core service layer
├── status/                           # Enums
│   └── ReadingStatus.java           # Reading status enum
└── util/                            # Utilities
    └── TempMultipartFile.java       # File handling
```

### Resources
- `application.properties`: Database and application configuration
- `schema.sql`: Database table definitions
- `data.sql`: Sample data initialization
- `templates/`: Thymeleaf HTML templates for web UI

### Test Structure
- `src/test/groovy/`: Spock framework tests (104 total)
- All tests should pass; investigate any new failures

## Common Troubleshooting

### Build Issues
- If builds fail with dependency issues, clear Maven cache: `rm -rf ~/.m2/repository`
- If tests fail unexpectedly, check if H2 database initialization is working
- Ensure Java 17 is being used: `java -version` (should show OpenJDK 17.0.16)
- If Maven wrapper fails: `chmod +x mvnw` to ensure executable permissions

### Runtime Issues  
- If port 8080 is busy: `lsof -ti:8080` to find processes, `pkill -f spring-boot:run` to stop
- If H2 console doesn't work: Check application.properties has `spring.h2.console.enabled=true`
- If Japanese text appears garbled: Ensure UTF-8 encoding in templates
- If application won't start: Check for "Address already in use" errors and kill existing processes

### Docker Issues
- Ensure JAR file exists before Docker build: `./mvnw clean package`
- If container won't start: Check Docker logs with `docker logs <container_name>`
- If Docker Compose fails: Try `docker compose down` then `docker compose up --build`

## Repository Files Overview

### Key Files
- `README.md`: Comprehensive documentation in Japanese
- `pom.xml`: Maven build configuration with all dependencies
- `Dockerfile`: Multi-stage Docker build setup
- `docker-compose.yml`: Simple service definition
- `.github/workflows/build.yml`: CI/CD pipeline

### Sample Output from Common Commands

#### ls -la (repository root)
```
total 76
-rw-r--r-- 1 runner docker   311 .gitignore
-rw-r--r-- 1 runner docker   524 Dockerfile  
-rw-r--r-- 1 runner docker  6795 README.md
-rw-r--r-- 1 runner docker   202 docker-compose.yml
-rwxr-xr-x 1 runner docker 10069 mvnw
-rw-r--r-- 1 runner docker  6607 mvnw.cmd
-rw-r--r-- 1 runner docker  4546 pom.xml
```

#### Application startup logs
```
Started Main in 2.995 seconds (process running for 3.3)
Tomcat started on port 8080 (http) with context path '/'
H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:testdb'
```

## Development Best Practices

- Always test both Maven and Docker deployment methods after changes
- Check all HTTP endpoints return expected content (not just status codes)
- Verify Japanese text displays correctly in web interface
- Run full test suite before committing changes
- Use H2 console to inspect database state when debugging

## Quick Reference Commands

### Essential Commands (Copy-Paste Ready)
```bash
# Build and test (most common workflow)
./mvnw clean package
java -jar target/readrico.jar

# Development server
./mvnw spring-boot:run

# Tests only
./mvnw test

# Docker deployment
./mvnw clean package -DskipTests
docker compose up --build

# Clean up
pkill -f spring-boot:run
docker compose down
```

### Status Check Commands
```bash
# Check if app is running
curl -I http://localhost:8080

# Check main page content
curl -s http://localhost:8080 | grep -o "<title>.*</title>"

# Verify build artifacts
ls -lh target/readrico.jar

# Check Java version
java -version
```
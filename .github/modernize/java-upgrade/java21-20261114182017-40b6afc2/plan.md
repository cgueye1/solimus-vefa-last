# Upgrade Plan: vefa (Wolof Biblio) (java21-20261114182017-40b6afc2)

- **Generated**: 2026-11-14 18:20:17 UTC
- **HEAD Branch**: main
- **HEAD Commit ID**: cca6316

## Available Tools

**JDKs**
- JDK 21: /usr/libexec/java_home -v 21 (openjdk version 21.0.11) (available)

**Build Tools**
- Maven Wrapper: 3.9.5 (via `.mvn/wrapper/maven-wrapper.properties`) ✅ (compatible with Java 21+)
- Maven Surefire Plugin: 2.22.2 → **<NEEDS_UPGRADE>** (older versions may have issues with Java 21; 3.0+ recommended)

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

- Automatic execution mode enabled
- Focus on minimal, necessary changes only
- Preserve security controls and functional behavior
- Git repository available for version control

## Options

- Working branch: appmod/java-upgrade-java21-20261114182017-40b6afc2
- Run tests before and after the upgrade: true

## Upgrade Goals

- **Primary Goal**: Upgrade Java runtime from 17 to 21 (latest LTS)
- **Framework**: Keep Spring Boot 3.2.0 (compatible with Java 21)
- **Database**: MySQL 8.0.27 (compatible with Java 21)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible/Notes |
|---|---|---|---|
| Java (JDK) | 17 | 21 | User requested LTS upgrade |
| Spring Boot | 3.2.0 | 3.2.0 | Already compatible with Java 21 |
| Maven Wrapper | 3.9.5 | 3.9.0 | ✅ Fully compatible with Java 21 |
| maven-compiler-plugin | 3.11.0 | 3.11.0 | ✅ Supports Java 21 |
| maven-surefire-plugin | 2.22.2 | 3.0+ | ⚠️ 2.22.2 is EOL; 3.0+ recommended for Java 17+ |
| MySQL Connector | 8.0.27 | 8.0.27 | ✅ Compatible with Java 21 |
| javax.activation-api | 1.2.0 | 1.2.0 | ✅ Bundled in Java 21 but explicit dep is OK for backward compat |
| Firebase Admin SDK | 9.1.1 | 9.1.1 | ✅ Compatible with Java 21 |
| Twilio SDK | 7.50.1 | 7.50.1 | ✅ Compatible with Java 21 |
| JJWT (JWT) | 0.11.5 | 0.11.5 | ✅ Compatible with Java 21 |
| AWS SDK | 2.26.0 | 2.26.0 | ✅ Compatible with Java 21 |

## Derived Upgrades

Based on Java 21 target, the following derived upgrades are recommended:

- **maven-surefire-plugin**: 2.22.2 → 3.0.0+ 
  - Reason: 2.22.2 is EOL; 3.0+ has better Java 17+ support and fixes module-system issues
  - Impact: Test execution will be more reliable on Java 21

## Impact Analysis

### Subsection: Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|---|---|---|---|---|---|
| pom.xml | java.version property | 17 | upgrade | 21 | User requested target version |
| pom.xml | maven-compiler-plugin source | 17 | upgrade | 21 | Align with target JDK |
| pom.xml | maven-compiler-plugin target | 17 | upgrade | 21 | Align with target JDK |
| pom.xml | maven-surefire-plugin | 2.22.2 | upgrade | 3.0.0 | EOL version; 3.0+ required for reliable Java 21 testing |
| Dockerfile | Base build image | maven:3.9-eclipse-temurin-17 | upgrade | maven:3.9-eclipse-temurin-21 | Match target JDK |
| Dockerfile | Runtime base image | eclipse-temurin:17-jre | upgrade | eclipse-temurin:21-jre | Match target JDK |

### Subsection: Source Code Changes

No source code changes required. The project:
- Uses only standard JDK modules (javax.crypto is part of java.base)
- Has no javax.servlet, javax.persistence, or other jakarta-namespace issues (Spring Boot 3.2 already handles Jakarta EE 10)
- Has no reflection-based access to internal JDK APIs
- Has no deprecated APIs removed in Java 21

### Subsection: Configuration Changes

No application configuration changes required:
- application.properties uses no deprecated or removed properties
- Spring Boot 3.2.0 BOM already includes all Java 21-compatible dependency versions

### Subsection: CI/CD Changes

| File | Location | Current | Required Change |
|---|---|---|---|
| Dockerfile | Line 1 | `FROM maven:3.9-eclipse-temurin-17 AS builder` | Change to: `FROM maven:3.9-eclipse-temurin-21 AS builder` |
| Dockerfile | Line 8 | `FROM eclipse-temurin:17-jre` | Change to: `FROM eclipse-temurin:21-jre` |

### Subsection: Risks & Warnings

**Low-risk upgrade**: Spring Boot 3.2.0 already supports Java 21, and no breaking changes are anticipated. This is primarily a configuration update.

- **maven-surefire-plugin version 2.22.2**: EOL and may have module-system compatibility issues with Java 21. Recommendation: upgrade to 3.0.0+. Mitigation: Follow Maven official release notes; 3.0.0+ is fully compatible with Java 21.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Verify Java 21 is available; prepare build environment
  - **Changes to Make**: No changes; verification only
  - **Verification**: `java -version` should return Java 21; `mvnw -version` should succeed

- Step 2: Setup Baseline (Optional)
  - **Rationale**: Establish baseline test results with current JDK 17 for comparison
  - **Changes to Make**: No changes; execute tests with current Java version
  - **Verification**: `mvnw clean test` with Java 17; document pass rate

- Step 3: Upgrade Java Version in pom.xml
  - **Rationale**: Update java.version property and compiler plugin source/target to Java 21
  - **Changes to Make**: 
    - Update `<java.version>17</java.version>` → `<java.version>21</java.version>`
    - Update maven-compiler-plugin `<source>17</source>` → `<source>21</source>`
    - Update maven-compiler-plugin `<target>17</target>` → `<target>21</target>`
  - **Verification**: `mvnw clean test-compile -q` with Java 21; verify compilation succeeds

- Step 4: Upgrade maven-surefire-plugin
  - **Rationale**: Update surefire to 3.0.0+ for better Java 21 compatibility and module-system support
  - **Changes to Make**:
    - Update maven-surefire-plugin version from 2.22.2 → 3.0.0
  - **Verification**: `mvnw clean test-compile -q` with Java 21; verify no new compilation errors

- Step 5: Update Docker Images
  - **Rationale**: Align Dockerfile with Java 21 target version for consistent build/runtime environments
  - **Changes to Make**:
    - Update Dockerfile build stage: `maven:3.9-eclipse-temurin-17` → `maven:3.9-eclipse-temurin-21`
    - Update Dockerfile runtime stage: `eclipse-temurin:17-jre` → `eclipse-temurin:21-jre`
  - **Verification**: `docker build -t vefa:java21 .` succeeds (if Docker available; otherwise skip)

- Step 6: Final Validation
  - **Rationale**: Verify all upgrade goals met; ensure 100% test pass rate with Java 21
  - **Changes to Make**: No code changes; execute full test suite
  - **Verification**: `mvnw clean test` with Java 21; achieve 100% test pass rate or ≥ baseline pass rate. Document any failures with root cause and fix.

---

**Notes**:
- Git repository available: yes (branch `main`, commit `cca6316`)
- Uncommitted changes detected: 4 modified files (handled via version control prepareBranch before execution)
- No Kotlin dependencies: Java version property only, no jvmTarget configuration needed
- Spring Boot 3.2.0 already supports Java 21: no Spring Boot version upgrade needed

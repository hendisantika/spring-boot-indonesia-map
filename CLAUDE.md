# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot application for displaying an interactive Indonesia map with administrative boundaries. Uses Spring Boot 4.0.3 with Java 25.

## Build & Run Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw clean spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=SpringBootIndonesiaMapApplicationTests

# Run a single test method
./mvnw test -Dtest=SpringBootIndonesiaMapApplicationTests#contextLoads
```

## Architecture

- **Base package**: `id.my.hendisantika.indonesiamap`
- **Build**: Maven with `mvnw` wrapper
- **Dependencies**: Spring Boot Starter, DevTools, Lombok, Spring Boot Test
- Lombok is used — annotate classes instead of writing boilerplate getters/setters/constructors
- `spring-boot-configuration-processor` is configured as an annotation processor

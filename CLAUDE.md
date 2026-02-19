# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.3 / Java 25 application displaying an interactive Indonesia map with administrative boundaries based on KEMENDAGRI 2025 data (Kepmendagri No 300.2.2-2138 Tahun 2025).

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
- Lombok is used — annotate classes instead of writing boilerplate
- `spring-boot-configuration-processor` is configured as an annotation processor

### Layers

- **model/**: DTOs for wilayah.id API responses (`Wilayah`, `WilayahResponse`, `ProvinceCoordinate`)
- **service/**: `WilayahService` (REST calls to wilayah.id API, cached with Caffeine), `BoundaryService` (loads province GeoJSON at startup, calculates centroids)
- **controller/**: `MapController` (Thymeleaf page), `WilayahApiController` (REST API), `WilayahFragmentController` (HTML fragments for dynamic dropdowns)
- **config/**: `AppConfig` (RestClient, ObjectMapper, caching)

### Data Flow

1. Province list fetched from `https://wilayah.id/api/provinces.json` on page load
2. Cascading dropdowns (Province > Kabupaten > Kecamatan > Desa) load via fragment endpoints
3. Province boundaries from static GeoJSON file (`static/geojson/indonesia-provinces.geojson`) with 38 provinces
4. Frontend uses Leaflet.js + W3.CSS, no build step needed

### File Header Convention

All classes (except main app and test) must include this header after imports:
```java
/**
 * Created by IntelliJ IDEA.
 * Project : indonesia-map
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 23/01/26
 * Time: 08.03
 * To change this template use File | Settings | File Templates.
 */
```

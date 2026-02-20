# Spring Boot Indonesia Map

Interactive Indonesia map application with complete administrative boundary polygons for all 4 levels of government administration, based on **KEMENDAGRI 2025** data (Kepmendagri No 300.2.2-2138 Tahun 2025).

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green)
![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-blue)

## Features

- Interactive map with boundary polygon display at every administrative level
- Cascading dropdown navigation: Provinsi > Kabupaten/Kota > Kecamatan > Desa/Kelurahan
- Single-boundary-per-selection display with auto-zoom
- Province markers on initial load
- Customizable color themes
- Detail panel showing administrative metadata (ibukota, luas, penduduk, elevasi, dll.)

## Boundary Data Coverage

| Level | Count | Coverage |
|-------|-------|----------|
| Provinsi | 38 | 100% |
| Kabupaten/Kota | 514 | 100% |
| Kecamatan | 7,061 | 99.9% |
| Desa/Kelurahan | 81,903 | 99.9% |

## Tech Stack

- **Backend**: Spring Boot 4.0.3, Java 25, Thymeleaf
- **Frontend**: Leaflet.js 1.9.4, W3.CSS, FontAwesome
- **Caching**: Caffeine (for wilayah.id API responses)
- **External API**: [wilayah.id](https://wilayah.id) for hierarchical administrative data
- **Boundary sources**: HDX (Humanitarian Data Exchange) WKB geometry + Indonesia wilayah SQL datasets

## Prerequisites

- Java 25+
- Maven (or use the included `mvnw` wrapper)

## Getting Started

```bash
# Clone the repository
git clone https://github.com/hendisantika/spring-boot-indonesia-map.git
cd spring-boot-indonesia-map

# Run the application
./mvnw clean spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

## Project Structure

```
src/main/
├── java/.../indonesiamap/
│   ├── config/          # RestClient, ObjectMapper, caching
│   ├── controller/      # MapController, WilayahApiController, WilayahFragmentController
│   ├── model/           # DTOs (Wilayah, WilayahResponse, ProvinceCoordinate)
│   └── service/         # WilayahService (API), BoundaryService (GeoJSON)
└── resources/
    ├── templates/       # Thymeleaf templates (index.html, fragments)
    └── static/geojson/
        ├── wilayah-boundaries.json   # Province & kabupaten boundaries (~8MB)
        ├── kecamatan/{kab_code}.json # 514 files with kecamatan polygons
        └── desa/{kab_code}.json      # 514 files with desa polygons
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /` | Main map page |
| `GET /wilayah/api/all` | All provinces with coordinates |
| `GET /wilayah/api/boundary/{code}` | Boundary data for a province or kabupaten |
| `GET /wilayah/api/boundaries/kabupaten/{provinceCode}` | All kabupaten boundaries in a province |
| `GET /wilayah/detail/{code}` | Detail fragment for any admin level |
| `GET /wilayah/children/{code}` | Dropdown fragment for child regions |

## Screenshots

After running the application, navigate to `http://localhost:8080` to see:
- Province-level map with clickable markers
- Kabupaten boundaries when selecting a province
- Kecamatan and desa boundaries when drilling down

## License

This project is open source.

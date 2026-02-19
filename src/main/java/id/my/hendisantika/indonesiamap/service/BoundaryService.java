package id.my.hendisantika.indonesiamap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class BoundaryService {

    private final ObjectMapper objectMapper;

    // Loaded from wilayah-boundaries.json (provinces + kabupaten)
    private final Map<String, JsonNode> wilayahData = new HashMap<>();

    // Loaded from indonesia-provinces.geojson (for initial map display)
    private final Map<String, JsonNode> provinceGeometries = new HashMap<>();
    private final Map<String, String> provinceNames = new HashMap<>();

    @PostConstruct
    public void init() {
        loadWilayahBoundaries();
        loadProvinceGeoJson();
    }

    private void loadWilayahBoundaries() {
        try {
            ClassPathResource resource = new ClassPathResource("static/geojson/wilayah-boundaries.json");
            try (InputStream is = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);
                root.fields().forEachRemaining(entry -> wilayahData.put(entry.getKey(), entry.getValue()));
            }
            long provinces = wilayahData.keySet().stream().filter(k -> !k.contains(".")).count();
            long kabupaten = wilayahData.keySet().stream().filter(k -> k.contains(".")).count();
            log.info("Loaded {} wilayah boundaries ({} provinces, {} kabupaten)", wilayahData.size(), provinces, kabupaten);
        } catch (Exception e) {
            log.error("Failed to load wilayah boundaries: {}", e.getMessage());
        }
    }

    private void loadProvinceGeoJson() {
        try {
            ClassPathResource resource = new ClassPathResource("static/geojson/indonesia-provinces.geojson");
            try (InputStream is = resource.getInputStream()) {
                JsonNode geoJson = objectMapper.readTree(is);
                JsonNode features = geoJson.get("features");
                if (features != null && features.isArray()) {
                    for (JsonNode feature : features) {
                        JsonNode props = feature.get("properties");
                        String code = props.get("KODE_PROV").asText();
                        String name = props.get("PROVINSI").asText();
                        JsonNode geometry = feature.get("geometry");
                        provinceGeometries.put(code, geometry);
                        provinceNames.put(code, name);
                    }
                }
            }
            log.info("Loaded {} province GeoJSON boundaries", provinceGeometries.size());
        } catch (Exception e) {
            log.error("Failed to load province GeoJSON: {}", e.getMessage());
        }
    }

    @Cacheable(value = "boundaries", key = "#code")
    public Map<String, Object> getBoundary(String code) {
        String normalized = code.replace(".", "");

        // Try wilayah-boundaries.json first (has provinces + kabupaten with polygon paths)
        JsonNode data = wilayahData.get(code);
        if (data != null && data.has("path")) {
            return buildBoundaryFromWilayah(code, data, normalized);
        }

        // Fallback to province GeoJSON for province-level
        if (normalized.length() == 2) {
            return getProvinceBoundaryFromGeoJson(code);
        }

        // For kecamatan/desa without boundaries, return parent province center
        String provinceCode = normalized.substring(0, 2);
        JsonNode provinceData = wilayahData.get(provinceCode);
        if (provinceData != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("kode", code);
            result.put("nama", provinceData.get("nama").asText());
            result.put("level", getLevelName(normalized));
            result.put("lat", provinceData.get("lat").asDouble());
            result.put("lng", provinceData.get("lng").asDouble());
            return result;
        }

        return new HashMap<>();
    }

    private Map<String, Object> buildBoundaryFromWilayah(String code, JsonNode data, String normalized) {
        Map<String, Object> result = new HashMap<>();
        result.put("kode", code);
        result.put("nama", data.get("nama").asText());
        result.put("level", getLevelName(normalized));
        result.put("lat", data.get("lat").asDouble());
        result.put("lng", data.get("lng").asDouble());

        // Convert path [[[lat,lng],...]] to the format frontend expects
        // Path is already in lat,lng order which Leaflet L.polygon() can use directly
        JsonNode path = data.get("path");
        if (path != null && path.isArray()) {
            List<List<List<Double>>> rings = new ArrayList<>();
            for (JsonNode ring : path) {
                List<List<Double>> points = new ArrayList<>();
                for (JsonNode point : ring) {
                    List<Double> coord = new ArrayList<>();
                    coord.add(point.get(0).asDouble()); // lat
                    coord.add(point.get(1).asDouble()); // lng
                    points.add(coord);
                }
                rings.add(points);
            }
            result.put("path", rings);
        }

        return result;
    }

    private Map<String, Object> getProvinceBoundaryFromGeoJson(String code) {
        Map<String, Object> result = new HashMap<>();
        result.put("kode", code);

        JsonNode geometry = provinceGeometries.get(code);
        if (geometry != null) {
            result.put("coordinates", geometry.toString());
            result.put("nama", provinceNames.getOrDefault(code, "Unknown"));
            result.put("level", "Provinsi");

            double[] center = calculateCenter(geometry);
            if (center != null) {
                result.put("lat", center[0]);
                result.put("lng", center[1]);
            }
        }

        return result;
    }

    /**
     * Get all kabupaten boundaries for a given province code.
     */
    public List<Map<String, Object>> getKabupatenBoundaries(String provinceCode) {
        List<Map<String, Object>> result = new ArrayList<>();
        String prefix = provinceCode + ".";
        for (Map.Entry<String, JsonNode> entry : wilayahData.entrySet()) {
            String kode = entry.getKey();
            if (kode.startsWith(prefix) && kode.length() <= 5) {
                JsonNode data = entry.getValue();
                Map<String, Object> item = buildBoundaryFromWilayah(kode, data, kode.replace(".", ""));
                result.add(item);
            }
        }
        return result;
    }

    private String getLevelName(String normalized) {
        return switch (normalized.length()) {
            case 2 -> "Provinsi";
            case 4 -> "Kabupaten/Kota";
            case 6 -> "Kecamatan";
            case 10 -> "Desa/Kelurahan";
            default -> "Wilayah";
        };
    }

    private double[] calculateCenter(JsonNode geometry) {
        if (geometry == null) return null;

        try {
            JsonNode coordinates = geometry.get("coordinates");
            double[] acc = new double[3];
            collectPoints(coordinates, acc);

            if (acc[2] > 0) {
                return new double[]{acc[0] / acc[2], acc[1] / acc[2]};
            }
        } catch (Exception e) {
            log.debug("Failed to calculate center: {}", e.getMessage());
        }
        return null;
    }

    private void collectPoints(JsonNode node, double[] acc) {
        if (node.isArray()) {
            if (node.size() == 2 && node.get(0).isNumber() && node.get(1).isNumber()) {
                acc[0] += node.get(1).asDouble(); // lat
                acc[1] += node.get(0).asDouble(); // lng
                acc[2] += 1;
            } else {
                for (JsonNode child : node) {
                    collectPoints(child, acc);
                }
            }
        }
    }

    public Map<String, double[]> getAllProvinceCenters() {
        Map<String, double[]> centers = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : wilayahData.entrySet()) {
            String kode = entry.getKey();
            if (!kode.contains(".")) {
                JsonNode data = entry.getValue();
                if (data.has("lat") && data.has("lng")) {
                    centers.put(kode, new double[]{data.get("lat").asDouble(), data.get("lng").asDouble()});
                }
            }
        }
        return centers;
    }

    public Map<String, String> getProvinceNames() {
        Map<String, String> names = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : wilayahData.entrySet()) {
            String kode = entry.getKey();
            if (!kode.contains(".") && entry.getValue().has("nama")) {
                names.put(kode, entry.getValue().get("nama").asText());
            }
        }
        return names;
    }
}

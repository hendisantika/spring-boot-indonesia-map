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
import java.util.HashMap;
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
    private final Map<String, JsonNode> provinceGeometries = new HashMap<>();
    private final Map<String, String> provinceNames = new HashMap<>();

    @PostConstruct
    public void init() {
        loadProvinceGeoJson();
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
            log.info("Loaded {} province boundaries", provinceGeometries.size());
        } catch (Exception e) {
            log.error("Failed to load province GeoJSON: {}", e.getMessage());
        }
    }

    @Cacheable(value = "boundaries", key = "#code")
    public Map<String, Object> getBoundary(String code) {
        String normalized = code.replace(".", "");

        if (normalized.length() == 2) {
            return getProvinceBoundary(code);
        }

        // For sub-province levels, return the parent province boundary as context
        String provinceCode = normalized.substring(0, 2);
        Map<String, Object> result = new HashMap<>();
        result.put("kode", code);

        JsonNode geometry = provinceGeometries.get(provinceCode);
        if (geometry != null) {
            result.put("coordinates", geometry.toString());
            result.put("nama", provinceNames.getOrDefault(provinceCode, "Unknown"));
            result.put("level", getLevelName(normalized));

            double[] center = calculateCenter(geometry);
            if (center != null) {
                result.put("lat", center[0]);
                result.put("lng", center[1]);
            }
        }

        return result;
    }

    private Map<String, Object> getProvinceBoundary(String code) {
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
        for (Map.Entry<String, JsonNode> entry : provinceGeometries.entrySet()) {
            double[] center = calculateCenter(entry.getValue());
            if (center != null) {
                centers.put(entry.getKey(), center);
            }
        }
        return centers;
    }

    public Map<String, String> getProvinceNames() {
        return new HashMap<>(provinceNames);
    }
}

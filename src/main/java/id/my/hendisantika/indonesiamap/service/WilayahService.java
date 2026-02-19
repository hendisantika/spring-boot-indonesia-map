package id.my.hendisantika.indonesiamap.service;

import id.my.hendisantika.indonesiamap.model.Wilayah;
import id.my.hendisantika.indonesiamap.model.WilayahResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

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
public class WilayahService {

    private final RestClient restClient;

    @Cacheable("provinces")
    public List<Wilayah> getProvinces() {
        try {
            WilayahResponse response = restClient.get()
                    .uri("/provinces.json")
                    .retrieve()
                    .body(WilayahResponse.class);
            return response != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch provinces: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "regencies", key = "#provinceCode")
    public List<Wilayah> getRegencies(String provinceCode) {
        try {
            WilayahResponse response = restClient.get()
                    .uri("/regencies/{code}.json", provinceCode)
                    .retrieve()
                    .body(WilayahResponse.class);
            return response != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch regencies for {}: {}", provinceCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "districts", key = "#regencyCode")
    public List<Wilayah> getDistricts(String regencyCode) {
        try {
            WilayahResponse response = restClient.get()
                    .uri("/districts/{code}.json", regencyCode)
                    .retrieve()
                    .body(WilayahResponse.class);
            return response != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch districts for {}: {}", regencyCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "villages", key = "#districtCode")
    public List<Wilayah> getVillages(String districtCode) {
        try {
            WilayahResponse response = restClient.get()
                    .uri("/villages/{code}.json", districtCode)
                    .retrieve()
                    .body(WilayahResponse.class);
            return response != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch villages for {}: {}", districtCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getLevelName(String code) {
        String normalized = code.replace(".", "");
        return switch (normalized.length()) {
            case 2 -> "Provinsi";
            case 4 -> "Kabupaten/Kota";
            case 6 -> "Kecamatan";
            case 10 -> "Desa/Kelurahan";
            default -> "Wilayah";
        };
    }

    public Wilayah findByCode(String code) {
        String normalized = code.replace(".", "");
        return switch (normalized.length()) {
            case 2 -> findInList(getProvinces(), code);
            case 4 -> findInList(getRegencies(code.substring(0, 2)), code);
            case 6 -> {
                String regencyCode = code.contains(".")
                        ? code.substring(0, code.indexOf(".", 3))
                        : code.substring(0, 4);
                yield findInList(getDistricts(regencyCode), code);
            }
            default -> {
                String districtCode = code.contains(".")
                        ? code.substring(0, code.lastIndexOf("."))
                        : code.substring(0, 6);
                yield findInList(getVillages(districtCode), code);
            }
        };
    }

    private Wilayah findInList(List<Wilayah> list, String code) {
        return list.stream()
                .filter(w -> w.getCode().equals(code))
                .findFirst()
                .orElse(new Wilayah(code, "Unknown"));
    }
}

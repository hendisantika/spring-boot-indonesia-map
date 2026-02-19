package id.my.hendisantika.indonesiamap.controller;

import id.my.hendisantika.indonesiamap.model.Wilayah;
import id.my.hendisantika.indonesiamap.service.BoundaryService;
import id.my.hendisantika.indonesiamap.service.WilayahService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/wilayah/api")
@RequiredArgsConstructor
public class WilayahApiController {

    private final WilayahService wilayahService;
    private final BoundaryService boundaryService;

    @GetMapping("/provinces")
    public ResponseEntity<List<Wilayah>> getProvinces() {
        return ResponseEntity.ok(wilayahService.getProvinces());
    }

    @GetMapping("/regencies/{provinceCode}")
    public ResponseEntity<List<Wilayah>> getRegencies(@PathVariable String provinceCode) {
        return ResponseEntity.ok(wilayahService.getRegencies(provinceCode));
    }

    @GetMapping("/districts/{regencyCode}")
    public ResponseEntity<List<Wilayah>> getDistricts(@PathVariable String regencyCode) {
        return ResponseEntity.ok(wilayahService.getDistricts(regencyCode));
    }

    @GetMapping("/villages/{districtCode}")
    public ResponseEntity<List<Wilayah>> getVillages(@PathVariable String districtCode) {
        return ResponseEntity.ok(wilayahService.getVillages(districtCode));
    }

    @GetMapping("/boundary/{code}")
    public ResponseEntity<Map<String, Object>> getBoundary(@PathVariable String code) {
        Map<String, Object> boundary = boundaryService.getBoundary(code);
        if (boundary.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(boundary);
    }

    @GetMapping("/detail/{code}")
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable String code) {
        Wilayah wilayah = wilayahService.findByCode(code);
        String level = wilayahService.getLevelName(code);

        Map<String, Object> detail = new HashMap<>();
        detail.put("kode", code);
        detail.put("nama", wilayah.getName());
        detail.put("level", level);

        return ResponseEntity.ok(detail);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, double[]> centers = boundaryService.getAllProvinceCenters();
        Map<String, String> names = boundaryService.getProvinceNames();

        List<Wilayah> provinces = wilayahService.getProvinces();
        for (Wilayah province : provinces) {
            Map<String, Object> item = new HashMap<>();
            item.put("kode", province.getCode());
            item.put("nama", province.getName());

            double[] center = centers.get(province.getCode());
            if (center != null) {
                item.put("lat", center[0]);
                item.put("lng", center[1]);
            }

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }
}

package id.my.hendisantika.indonesiamap.controller;

import id.my.hendisantika.indonesiamap.model.Wilayah;
import id.my.hendisantika.indonesiamap.service.WilayahService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
@Controller
@RequestMapping("/wilayah")
@RequiredArgsConstructor
public class WilayahFragmentController {

    private final WilayahService wilayahService;

    @GetMapping("/kabupaten-select/{provinceCode}")
    public String kabupatenSelect(@PathVariable String provinceCode, Model model) {
        List<Wilayah> regencies = wilayahService.getRegencies(provinceCode);
        model.addAttribute("items", regencies);
        model.addAttribute("type", "kabupaten");
        model.addAttribute("label", "Kabupaten/Kota");
        model.addAttribute("icon", "fa-city");
        model.addAttribute("onChange", "handleKabupatenChange");
        return "fragments/select-dropdown";
    }

    @GetMapping("/kecamatan-select/{regencyCode}")
    public String kecamatanSelect(@PathVariable String regencyCode, Model model) {
        List<Wilayah> districts = wilayahService.getDistricts(regencyCode);
        model.addAttribute("items", districts);
        model.addAttribute("type", "kecamatan");
        model.addAttribute("label", "Kecamatan");
        model.addAttribute("icon", "fa-building");
        model.addAttribute("onChange", "handleKecamatanChange");
        return "fragments/select-dropdown";
    }

    @GetMapping("/desa-select/{districtCode}")
    public String desaSelect(@PathVariable String districtCode, Model model) {
        List<Wilayah> villages = wilayahService.getVillages(districtCode);
        model.addAttribute("items", villages);
        model.addAttribute("type", "desa");
        model.addAttribute("label", "Desa/Kelurahan");
        model.addAttribute("icon", "fa-home");
        model.addAttribute("onChange", "handleDesaChange");
        return "fragments/select-dropdown";
    }

    @GetMapping("/detail/{code}")
    public String detail(@PathVariable String code, Model model) {
        Wilayah wilayah = wilayahService.findByCode(code);
        String level = wilayahService.getLevelName(code);
        model.addAttribute("wilayah", wilayah);
        model.addAttribute("level", level);
        return "fragments/detail-panel";
    }
}

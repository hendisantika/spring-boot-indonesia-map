package id.my.hendisantika.indonesiamap.controller;

import id.my.hendisantika.indonesiamap.service.WilayahService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
@RequiredArgsConstructor
public class MapController {

    private final WilayahService wilayahService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("provinces", wilayahService.getProvinces());
        return "index";
    }
}

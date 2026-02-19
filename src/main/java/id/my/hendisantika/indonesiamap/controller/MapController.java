package id.my.hendisantika.indonesiamap.controller;

import id.my.hendisantika.indonesiamap.service.WilayahService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-indonesia-map
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 20/02/26
 * Time: 05.43
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

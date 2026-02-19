package id.my.hendisantika.indonesiamap.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WilayahResponse {
    private List<Wilayah> data;
    private Meta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private int administrative_area_level;
        private String updated_at;
    }
}

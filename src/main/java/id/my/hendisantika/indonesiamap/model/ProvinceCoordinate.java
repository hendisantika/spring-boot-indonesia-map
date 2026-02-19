package id.my.hendisantika.indonesiamap.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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
@AllArgsConstructor
public class ProvinceCoordinate {
    private String code;
    private String name;
    private double latitude;
    private double longitude;
}

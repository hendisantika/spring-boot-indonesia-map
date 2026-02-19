package id.my.hendisantika.indonesiamap.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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
@Data
@AllArgsConstructor
public class ProvinceCoordinate {
    private String code;
    private String name;
    private double latitude;
    private double longitude;
}

package id.my.hendisantika.indonesiamap.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
public class PinnedPlace {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String category;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

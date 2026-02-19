package id.my.hendisantika.indonesiamap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

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
@Configuration
@EnableCaching
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://wilayah.id/api")
                .build();
    }
}

package id.my.hendisantika.indonesiamap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.my.hendisantika.indonesiamap.model.PinnedPlace;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
public class PinnedPlaceService {

    private final ObjectMapper objectMapper;
    private final Path filePath;

    public PinnedPlaceService(ObjectMapper objectMapper,
                              @Value("${app.pins.file-path:./data/pinned-places.json}") String filePath) {
        this.objectMapper = objectMapper;
        this.filePath = Path.of(filePath);
    }

    @PostConstruct
    public void init() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
            log.info("Created data directory: {}", parent);
        }
        if (!Files.exists(filePath)) {
            objectMapper.writeValue(filePath.toFile(), new ArrayList<>());
            log.info("Created empty pins file: {}", filePath);
        }
    }

    public List<PinnedPlace> getAll() {
        try {
            return objectMapper.readValue(filePath.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to read pins file: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Optional<PinnedPlace> getById(String id) {
        return getAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public PinnedPlace create(PinnedPlace pin) {
        List<PinnedPlace> pins = getAll();
        pin.setId(UUID.randomUUID().toString());
        pin.setCreatedAt(LocalDateTime.now());
        pin.setUpdatedAt(LocalDateTime.now());
        pins.add(pin);
        save(pins);
        log.info("Created pin: {} at [{}, {}]", pin.getName(), pin.getLatitude(), pin.getLongitude());
        return pin;
    }

    public Optional<PinnedPlace> update(String id, PinnedPlace updated) {
        List<PinnedPlace> pins = getAll();
        for (int i = 0; i < pins.size(); i++) {
            if (pins.get(i).getId().equals(id)) {
                updated.setId(id);
                updated.setCreatedAt(pins.get(i).getCreatedAt());
                updated.setUpdatedAt(LocalDateTime.now());
                pins.set(i, updated);
                save(pins);
                log.info("Updated pin: {}", id);
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public boolean delete(String id) {
        List<PinnedPlace> pins = getAll();
        boolean removed = pins.removeIf(p -> p.getId().equals(id));
        if (removed) {
            save(pins);
            log.info("Deleted pin: {}", id);
        }
        return removed;
    }

    private void save(List<PinnedPlace> pins) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), pins);
        } catch (IOException e) {
            log.error("Failed to write pins file: {}", e.getMessage());
        }
    }
}

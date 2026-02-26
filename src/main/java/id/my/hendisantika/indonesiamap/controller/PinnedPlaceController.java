package id.my.hendisantika.indonesiamap.controller;

import id.my.hendisantika.indonesiamap.model.PinnedPlace;
import id.my.hendisantika.indonesiamap.service.PinnedPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinnedPlaceController {

    private final PinnedPlaceService pinnedPlaceService;

    @GetMapping
    public ResponseEntity<List<PinnedPlace>> getAll() {
        return ResponseEntity.ok(pinnedPlaceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PinnedPlace> getById(@PathVariable String id) {
        return pinnedPlaceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PinnedPlace> create(@RequestBody PinnedPlace pin) {
        PinnedPlace created = pinnedPlaceService.create(pin);
        return ResponseEntity.created(URI.create("/api/pins/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PinnedPlace> update(@PathVariable String id, @RequestBody PinnedPlace pin) {
        return pinnedPlaceService.update(id, pin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (pinnedPlaceService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

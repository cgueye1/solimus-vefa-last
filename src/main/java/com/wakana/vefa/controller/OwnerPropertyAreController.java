package com.wakana.vefa.controller;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wakana.vefa.services.OwnerPropertyAreaService;

@RestController
@RequestMapping("/api/ownerpropertyare")
@RequiredArgsConstructor
public class OwnerPropertyAreController {

    @Autowired
    private OwnerPropertyAreaService ownerPropertyAreaService;

    @GetMapping("/save/{pid}/{uid}/{area}")
    public ResponseEntity<?> save(@PathVariable Long pid, @PathVariable Long uid, @PathVariable double area) {
        return ResponseEntity.ok(ownerPropertyAreaService.add(pid, uid, area));

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            ownerPropertyAreaService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete album: " + e.getMessage());
        }
    }

}

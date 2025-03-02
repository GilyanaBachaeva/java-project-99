package hexlet.code.demo.controller;

import hexlet.code.demo.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.demo.dto.LabelDTO.LabelDTO;
import hexlet.code.demo.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.demo.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public final class LabelController {
    private final LabelService labelService;

    @GetMapping()
    public ResponseEntity<List<LabelDTO>> index() {
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labelService.getAll().size()))
                .body(labelService.getAll());
    }

    @GetMapping("/{id}")
    public LabelDTO show(@PathVariable Long id) {
        return labelService.findById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO labelData) {
        LabelDTO result = labelService.create(labelData);
        return result;
    }

    @PutMapping("/{id}")
    public LabelDTO update(@Valid @RequestBody LabelUpdateDTO labelData, @PathVariable Long id) {
        LabelDTO result = labelService.update(labelData, id);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        labelService.delete(id);
    }
}

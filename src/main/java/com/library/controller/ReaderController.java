package com.library.controller;

import com.library.dto.ReaderDtos.Request;
import com.library.dto.ReaderDtos.Response;
import com.library.service.ReaderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {

    private final ReaderService service;

    public ReaderController(ReaderService service) { this.service = service; }

    @GetMapping
    public Page<Response> list(@RequestParam(required = false) String q,
                               @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return service.search(q, pageable);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody Request req) { return service.create(req); }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody Request req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}

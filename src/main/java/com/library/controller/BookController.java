package com.library.controller;

import com.library.dto.BookDtos.Request;
import com.library.dto.BookDtos.Response;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) { this.service = service; }

    /** Поиск (q — название/автор/ISBN), фильтр onlyAvailable, сортировка и пагинация: ?sort=title,asc&page=0&size=20 */
    @GetMapping
    public Page<Response> list(@RequestParam(required = false) String q,
                               @RequestParam(defaultValue = "false") boolean onlyAvailable,
                               @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return service.search(q, onlyAvailable, pageable);
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

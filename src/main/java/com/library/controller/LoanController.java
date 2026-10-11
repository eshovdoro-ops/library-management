package com.library.controller;

import com.library.dto.LoanDtos.Filter;
import com.library.dto.LoanDtos.IssueRequest;
import com.library.dto.LoanDtos.Response;
import com.library.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) { this.service = service; }

    /** Фильтр status: ALL | ACTIVE | OVERDUE | RETURNED */
    @GetMapping
    public Page<Response> list(@RequestParam(defaultValue = "ALL") Filter status,
                               @PageableDefault(size = 20, sort = "issuedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, pageable);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response issue(@Valid @RequestBody IssueRequest req) {
        return service.issue(req.bookId(), req.readerId());
    }

    @PostMapping("/{id}/return")
    public Response returnBook(@PathVariable Long id) { return service.returnBook(id); }
}

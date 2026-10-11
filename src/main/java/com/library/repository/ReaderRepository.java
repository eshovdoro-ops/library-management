package com.library.repository;

import com.library.entity.Reader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReaderRepository extends JpaRepository<Reader, Long> {

    @Query("""
           select r from Reader r
           where lower(r.fullName) like :pattern or lower(r.email) like :pattern
           """)
    Page<Reader> search(@Param("pattern") String pattern, Pageable pageable);
}
